package protocol.server;

import protocol.*;
import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.MlkemImple;
import protocol.polynomial.NttImple;
import protocol.polynomial.Polynomial;
import protocol.random.RandomCustom;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static protocol.polynomial.Utils.*;

public class ServerImple implements Server {

    private final int n;
    private final BigInteger q;
    private final int eta;
    private final ProtocolConfiguration protocolConfiguration;
    private final EngineImple engine;
    private final MlkemImple mlkem;
    private final NttImple ntt;
    private final Ding12Imple magic;
    private final SessionConfiguration sessionConfiguration = new SessionConfiguration();

    public ServerImple(RandomCustom random, int n, BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        this.engine = new EngineImple(random);
        this.protocolConfiguration = new ProtocolConfiguration(this.n, this.q, this.eta);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
        this.magic = new Ding12Imple(this.q);
    }

    @Override
    public ProtocolConfiguration getProtocolConfiguration() {
        return protocolConfiguration;
    }

    @Override
    public void enrollClient(byte[] publicSeedForA, byte[] I, byte[] salt, Polynomial vNtt) {
        ServersDatabase.saveClient(new ByteArrayWrapper(I.clone()), new ClientRecord(publicSeedForA.clone(), salt.clone(), vNtt.defensiveCopy()));
    }

    @Override
    public SaltEphPublicSignal computeSharedSecret(byte[] I, Polynomial piNtt) throws NotEnrolledClientException {
        ByteArrayWrapper wrappedIdentity = new ByteArrayWrapper(I.clone());
        sessionConfiguration.setClientsEphPubKey(piNtt.defensiveCopy());
        Polynomial constantTwoPolyNtt = Polynomial.constantTwoNtt(n, q);
        // Extract database. //
        if (!ServersDatabase.contains(wrappedIdentity)) {
            throw new NotEnrolledClientException("Identity " + Arrays.toString(I) + " not found in the database.");
        }
        byte[] publicSeedForA = ServersDatabase.getClient(wrappedIdentity).getPublicSeedForA();
        Polynomial vNtt = ServersDatabase.getClient(wrappedIdentity).getVerifierNtt();
        byte[] salt = ServersDatabase.getClient(wrappedIdentity).getSalt();
        // pj = as1' + 2e1' + v //
        // Create polynomial a from public seed.
        List<BigInteger> aNttCoeffs = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformCoeffsNtt(engine, aNttCoeffs, publicSeedForA);
        Polynomial aNtt = new Polynomial(List.copyOf(aNttCoeffs), q, true);
        // Compute s1'.
        Polynomial s1Prime = generateRandomErrorPoly(protocolConfiguration, mlkem, engine);
        Polynomial s1PrimeNtt = ntt.convertToNtt(s1Prime.defensiveCopy());
        // Compute e1'.
        Polynomial e1Prime = generateRandomErrorPoly(protocolConfiguration, mlkem, engine);
        Polynomial e1PrimeNtt = ntt.convertToNtt(e1Prime.defensiveCopy());
        // Do all the math.
        Polynomial summedFstTwoTuples = multiply2NttTuplesAddThemTogetherNtt(aNtt, s1PrimeNtt, constantTwoPolyNtt, e1PrimeNtt);
        Polynomial pjNtt = summedFstTwoTuples.add(vNtt);
        sessionConfiguration.setServersEphPubKey(pjNtt.defensiveCopy());
        // u = XOF(H(pi || pj)) //
        Polynomial uNtt = computeUNtt(protocolConfiguration, engine, mlkem, piNtt, pjNtt);
        // kj = (v + pi)s1' + uv + 2e1''' //
        // Compute e1'''.
        Polynomial e1TriplePrime = generateRandomErrorPoly(protocolConfiguration, mlkem, engine);
        Polynomial e1TriplePrimeNtt = ntt.convertToNtt(s1Prime.defensiveCopy());
        // Do all the math.
        Polynomial bracket = vNtt.add(piNtt);
        Polynomial kjNtt = multiply3NttTuplesAndAddThemTogetherNtt(bracket, s1PrimeNtt, uNtt, vNtt, constantTwoPolyNtt, e1TriplePrimeNtt);
        Polynomial kj = ntt.convertFromNtt(kjNtt.defensiveCopy());
        // wj = Cha(kj) //
        List<Integer> wj = IntStream.range(0, n).mapToObj(i -> magic.signalFunction(engine, kj.getCoeffs().get(i))).toList();
        // sigmaj = Mod_2(kj, wj) //
        List<Integer> sigmaj = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(kj.getCoeffs().get(i), wj.get(i))).toList();
        // skj = SHA3-256(sigmaj) //
        //System.out.println(sigmaj);
        sessionConfiguration.setSharedSecret(Utils.convertIntegerListToByteArrayAndHashIt(n, engine, sigmaj));

        return new SaltEphPublicSignal(salt.clone(), pjNtt.defensiveCopy(), wj);
    }

    @Override
    public byte[] verifyEntities(byte[] m1) throws ClientNotAuthenticatedException {
        Polynomial piNtt = sessionConfiguration.getClientsEphPubKey();
        Polynomial pjNtt = sessionConfiguration.getServersEphPubKey();
        byte[] skj = sessionConfiguration.getSharedSecret();
        // M1' = SHA3-256(pi || pj || skj) //
        byte[] m1Prime = Utils.concatenateTwoByteArraysAndHashThem(engine, piNtt.concatWith(pjNtt).toByteArray(), skj);
        // VERIFY that M1 == M1'. If true, return M2', else return empty byte array.
        ByteArrayWrapper m1Wrapped = new ByteArrayWrapper(m1);
        ByteArrayWrapper m1PrimeWrapped = new ByteArrayWrapper(m1Prime);
        if (!m1Wrapped.equals(m1PrimeWrapped)) {
            throw new ClientNotAuthenticatedException("M1 does not equal to M1'.");
        }
        // M2' = SHA3-256(pi || M1' || skj) //
        return Utils.concatenateThreeByteArraysAndHash(engine, piNtt.toByteArray(), m1Prime, skj);
    }
}
