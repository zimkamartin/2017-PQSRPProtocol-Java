package protocol.server;

import protocol.*;
import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.polynomial.NttImple;
import protocol.random.RandomCustom;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static protocol.Utils.computeUNtt;

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
    public ProtocolConfiguration getPublicParams() {
        return protocolConfiguration;
    }

    @Override
    public void enrollClient(byte[] publicSeedForA, byte[] I, byte[] salt, List<BigInteger> vNtt) {
        ServersDatabase.saveClient(new ByteArrayWrapper(I.clone()), new ClientRecord(publicSeedForA.clone(), salt.clone(), List.copyOf(vNtt)));
    }

    @Override
    public SaltEphPublicSignal computeSharedSecret(byte[] I, List<BigInteger> piNtt) throws NotEnrolledClientException {
        ByteArrayWrapper wrappedIdentity = new ByteArrayWrapper(I.clone());
        sessionConfiguration.setClientsEphPubKey(List.copyOf(piNtt));
        List<BigInteger> constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // Extract database. //
        if (!ServersDatabase.contains(wrappedIdentity)) {
            throw new NotEnrolledClientException("Identity " + Arrays.toString(I) + " not found in the database.");
        }
        byte[] publicSeedForA = ServersDatabase.getClient(wrappedIdentity).getPublicSeedForA();
        List<BigInteger> vNtt = ServersDatabase.getClient(wrappedIdentity).getVerifierNtt();
        byte[] salt = ServersDatabase.getClient(wrappedIdentity).getSalt();
        // pj = as1' + 2e1' + v //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Compute s1'.
        List<BigInteger> s1PrimeNtt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Compute e1'.
        List<BigInteger> e1PrimeNtt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Do all the math.
        List<BigInteger> pjNtt = ntt.addPolys(Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, s1PrimeNtt, constantTwoPolyNtt, e1PrimeNtt), vNtt);
        sessionConfiguration.setServersEphPubKey(List.copyOf(pjNtt));
        // u = XOF(H(pi || pj)) //
        List<BigInteger> uNtt = computeUNtt(engine, mlkem, n, piNtt, pjNtt);
        // kj = (v + pi)s1' + uv + 2e1''' //
        // Compute e1'''.
        List<BigInteger> e1TriplePrimeNtt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Do all the math.
        List<BigInteger> bracket = ntt.addPolys(vNtt, piNtt);
        List<BigInteger> kj = Utils.multiply3NttTuplesAndAddThemTogether(ntt, bracket, s1PrimeNtt, uNtt, vNtt, constantTwoPolyNtt, e1TriplePrimeNtt);
        // wj = Cha(kj) //
        List<Integer> wj = IntStream.range(0, n).mapToObj(i -> magic.signalFunction(engine, kj.get(i))).toList();
        // sigmaj = Mod_2(kj, wj) //
        List<Integer> sigmaj = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(kj.get(i), wj.get(i))).toList();
        // skj = SHA3-256(sigmaj) //
        //System.out.println(sigmaj);
        sessionConfiguration.setSharedSecret(Utils.convertIntegerListToByteArrayAndHashIt(n, engine, sigmaj));

        return new SaltEphPublicSignal(salt.clone(), List.copyOf(pjNtt), List.copyOf(wj));
    }

    @Override
    public byte[] verifyEntities(byte[] m1) throws ClientNotAuthenticatedException {
        List<BigInteger> piNtt = sessionConfiguration.getClientsEphPubKey();
        List<BigInteger> pjNtt = sessionConfiguration.getServersEphPubKey();
        byte[] skj = sessionConfiguration.getSharedSecret();
        // M1' = SHA3-256(pi || pj || skj) //
        byte[] m1Prime = Utils.concatenateTwoByteArraysAndHashThem(engine, Utils.concatBigIntegerListsToByteArray(piNtt, pjNtt), skj);
        // VERIFY that M1 == M1'. If true, return M2', else return empty byte array.
        ByteArrayWrapper m1Wrapped = new ByteArrayWrapper(m1);
        ByteArrayWrapper m1PrimeWrapped = new ByteArrayWrapper(m1Prime);
        if (!m1Wrapped.equals(m1PrimeWrapped)) {
            throw new ClientNotAuthenticatedException("M1 does not equal to M1'.");
        }
        // M2' = SHA3-256(pi || M1' || skj) //
        return Utils.concatenateThreeByteArraysAndHash(engine, Utils.convertBigIntegerListToByteArray(piNtt), m1Prime, skj);
    }
}
