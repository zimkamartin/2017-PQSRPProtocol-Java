package protocol;

import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.exceptions.ServerNotAuthenticatedException;
import protocol.random.RandomCustom;
import protocol.server.Server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static protocol.Utils.computeUNtt;

public class ClientImple {

    private static final int PUBLICSEEDFORASIZE = 34;  // Size could be changed however you wish.
    private static final int SALTSIZE = 34;  // Size could be changed however you wish.

    private final Server server;
    private final ProtocolConfiguration protocolConfiguration;
    private final int n;
    private final BigInteger q;
    private final int eta;
    private final byte[] publicSeedForA = new byte[PUBLICSEEDFORASIZE];
    private final EngineImple engine;
    private final MlkemImple mlkem;
    private final NttImple ntt;
    private final MagicImple magic;
    private final SessionConfiguration sessionConfiguration = new SessionConfiguration();

    public ClientImple(RandomCustom random, Server server) {
        this.server = server;
        this.protocolConfiguration = server.getPublicParams();
        this.n = this.protocolConfiguration.getN();
        this.q = this.protocolConfiguration.getQ();
        this.eta = this.protocolConfiguration.getEta();
        this.engine = new EngineImple(random);
        this.engine.getRandomBytes(this.publicSeedForA);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
        this.magic = new MagicImple(this.q);
    }

    private byte[] computeSeed1(ClientsSecrets cs, byte[] salt) {
        byte[] identity = cs.getIdentity();
        byte[] password = cs.getPassword();
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd)) //
        byte[] innerHash = Utils.concatenateTwoByteArraysAndHashThem(engine, identity, password);
        return Utils.concatenateTwoByteArraysAndHashThem(engine, salt, innerHash);
    }

    private List<BigInteger> computeVNttFromANttAndSalt(ClientsSecrets cs, List<BigInteger> aNtt, byte[] salt) {
        // v = asv + 2ev //
        // Compute seeds.
        byte[] seed1 = computeSeed1(cs, salt);
        byte[] seed2 = new byte[32];
        engine.hash(seed2, seed1);
        // Based on seeds (computed from private values) generate sv, ev.
        List<BigInteger> sv = new ArrayList<>(Collections.nCopies(n, null));
        List<BigInteger> ev = new ArrayList<>(Collections.nCopies(n, null));
        Utils.getEtaNoise(protocolConfiguration, mlkem, engine, sv, seed1);
        Utils.getEtaNoise(protocolConfiguration, mlkem, engine, ev, seed2);
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        List<BigInteger> evNtt = ntt.convertToNtt(ev);
        // Do all the math.
        return Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, svNtt, ntt.generateConstantTwoPolynomialNtt(), evNtt);
    }

    private void computeSharedSecret(ClientsSecrets cs) throws NotEnrolledClientException {
        List<BigInteger> constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // pi = as1 + 2e1 //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Compute s1.
        List<BigInteger> s1Ntt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Compute e1.
        List<BigInteger> e1Ntt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Do all the math.
        List<BigInteger> piNtt = Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, s1Ntt, constantTwoPolyNtt, e1Ntt);
        sessionConfiguration.setClientsEphPubKey(List.copyOf(piNtt));
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(cs.getIdentity(), sessionConfiguration.getClientsEphPubKey());
        byte[] salt = sPjNttWj.getSalt();
        sessionConfiguration.setServersEphPubKey(List.copyOf(sPjNttWj.getPjNtt()));
        List<Integer> wj = sPjNttWj.getWj();
        // u = XOF(H(pi || pj)) //
        List<BigInteger> uNtt = computeUNtt(engine, mlkem, n, piNtt, sessionConfiguration.getServersEphPubKey());
        // v = asv + 2ev //
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        List<BigInteger> e1DoublePrimeNtt = Utils.generateRandomErrorPolyNtt(protocolConfiguration, mlkem, engine, ntt);
        // Compute sv.
        List<BigInteger> sv = new ArrayList<>(Collections.nCopies(n, null));
        Utils.getEtaNoise(protocolConfiguration, mlkem, engine, sv, computeSeed1(cs, salt));
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        // Do all the math.
        List<BigInteger> fstBracket = ntt.subtractPolys(sessionConfiguration.getServersEphPubKey(), vNtt);
        List<BigInteger> sndBracket = ntt.addPolys(svNtt, s1Ntt);
        List<BigInteger> ki = Utils.multiply3NttTuplesAndAddThemTogether(ntt, fstBracket, sndBracket, uNtt, vNtt, constantTwoPolyNtt, e1DoublePrimeNtt);
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(ki.get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        //System.out.println(sigmai);
        sessionConfiguration.setSharedSecret(Utils.convertIntegerListToByteArrayAndHashIt(n, engine, sigmai));
    }

    private byte[] verifyEntities() throws ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        List<BigInteger> piNtt = sessionConfiguration.getClientsEphPubKey();
        List<BigInteger> pjNtt = sessionConfiguration.getServersEphPubKey();
        byte[] ski = sessionConfiguration.getSharedSecret();
        // M1 = SHA3-256(pi || pj || ski) //
        byte[] m1 = Utils.concatenateTwoByteArraysAndHashThem(engine, Utils.concatBigIntegerListsToByteArray(piNtt, pjNtt), ski);
        // M2 = SHA3-256(pi || M1 || ski) //
        byte[] m2Prime = server.verifyEntities(m1);
        byte[] m2 = Utils.concatenateThreeByteArraysAndHash(engine, Utils.convertBigIntegerListToByteArray(piNtt), m1, ski);
        // VERIFY that M2 == M2'. If true, return key.
        ByteArrayWrapper m2Wrapped = new ByteArrayWrapper(m2);
        ByteArrayWrapper m2PrimeWrapped = new ByteArrayWrapper(m2Prime);
        if (!m2Wrapped.equals(m2PrimeWrapped)) {
            throw new ServerNotAuthenticatedException("M2 does not equal to M2'.");
        }
        return ski;
    }

    public void enroll(ClientsSecrets cs) {
        // PHASE 0 //
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Generate salt.
        byte[] salt = new byte[SALTSIZE];
        engine.getRandomBytes(salt);
        // Compute v.
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
        // Send public seed for a, identity, salt and v in NTT form to the server. //
        server.enrollClient(publicSeedForA, cs.getIdentity(), salt, vNtt);
    }

    public byte[] login(ClientsSecrets cs) throws NotEnrolledClientException, ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        // PHASE 1 //
        computeSharedSecret(cs);
        // PHASE 2 //
        return verifyEntities();
    }
}
