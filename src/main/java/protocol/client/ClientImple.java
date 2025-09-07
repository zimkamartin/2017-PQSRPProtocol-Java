package protocol.client;

import protocol.*;
import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.exceptions.ServerNotAuthenticatedException;
import protocol.polynomial.ClassicalPolynomial;
import protocol.polynomial.NttImple;
import protocol.polynomial.NttPolynomial;
import protocol.random.RandomCustom;
import protocol.server.Server;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

import static protocol.polynomial.Utils.*;

public class ClientImple {

    private static final int PUBLICSEEDFORASIZE = 34;  // Size could be changed however you wish.
    private static final int SALTSIZE = 34;  // Size could be changed however you wish.

    private final Server server;
    private final ProtocolConfiguration protocolConfiguration;
    private final RandomCustom randomCustomImple;
    private final int n;
    private final BigInteger q;
    private final byte[] publicSeedForA = new byte[PUBLICSEEDFORASIZE];
    private final EngineImple engine;
    private final NttImple ntt;
    private final Ding12Imple ding12;
    private final SessionConfiguration sessionConfiguration = new SessionConfiguration();

    public ClientImple(RandomCustom random, Server server) {
        this.server = server;
        this.protocolConfiguration = server.getProtocolConfiguration();
        this.n = this.protocolConfiguration.getN();
        this.q = this.protocolConfiguration.getQ();
        this.randomCustomImple = random;
        this.randomCustomImple.getRandomBytes(this.publicSeedForA);
        this.engine = new EngineImple();
        this.ntt = new NttImple(this.n, this.q);
        this.ding12 = new Ding12Imple(this.q);
    }

    private byte[] computeSeed1(ClientsSecrets cs, byte[] salt) {
        byte[] identity = cs.getIdentity();
        byte[] password = cs.getPassword();
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd)) //
        byte[] innerHash = Utils.concatenateTwoByteArraysAndHashThem(engine, identity, password);
        return Utils.concatenateTwoByteArraysAndHashThem(engine, salt, innerHash);
    }

    private NttPolynomial computeVNttFromANttAndSalt(ClientsSecrets cs, NttPolynomial aNtt, byte[] salt) {
        // v = asv + 2ev //
        // Compute seeds.
        byte[] seed1 = computeSeed1(cs, salt);
        byte[] seed2 = new byte[32];
        engine.hash(seed2, seed1);
        // Based on seeds (computed from private values) generate sv, ev.
        NttPolynomial svNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), seed1);
        NttPolynomial evNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), seed2);
        // Do all the math.
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, q);
        return multiply2NttTuplesAddThemTogetherNtt(aNtt.defensiveCopy(), svNtt, constantTwoPolyNtt, evNtt);
    }

    private void computeSharedSecret(ClientsSecrets cs) throws NotEnrolledClientException {
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, q);
        // pi = as1 + 2e1 //
        // Create polynomial a from public seed.
        NttPolynomial aNtt = generateUniformPolyNtt(protocolConfiguration, randomCustomImple, publicSeedForA);
        // Compute s1.
        NttPolynomial s1Ntt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray());
        // Compute e1.
        NttPolynomial e1Ntt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray());
        // Do all the math.
        NttPolynomial piNtt = multiply2NttTuplesAddThemTogetherNtt(aNtt, s1Ntt, constantTwoPolyNtt, e1Ntt);
        sessionConfiguration.setClientsEphPubKey(piNtt.defensiveCopy());
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(cs.getIdentity(), sessionConfiguration.getClientsEphPubKey());
        byte[] salt = sPjNttWj.getSalt();
        sessionConfiguration.setServersEphPubKey(sPjNttWj.getPjNtt().defensiveCopy());
        List<Integer> wj = sPjNttWj.getWj();
        // u = XOF(H(pi || pj)) //
        NttPolynomial uNtt = computeUNtt(protocolConfiguration, engine, randomCustomImple, piNtt.defensiveCopy(), sessionConfiguration.getServersEphPubKey());
        // v = asv + 2ev //
        NttPolynomial vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        NttPolynomial e1DoublePrimeNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray());
        // Compute sv.
        NttPolynomial svNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), computeSeed1(cs, salt));
        // Do all the math.
        NttPolynomial fstBracket = sessionConfiguration.getServersEphPubKey().subtract(vNtt);
        NttPolynomial sndBracket = svNtt.add(s1Ntt);
        ClassicalPolynomial ki = multiply3NttTuplesAndAddThemTogether(fstBracket, sndBracket, uNtt, vNtt, constantTwoPolyNtt, e1DoublePrimeNtt, ntt.getZetasInvertedArray());
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> ding12.robustExtractor(ki.getCoeffs().get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        //System.out.println(sigmai);
        sessionConfiguration.setSharedSecret(Utils.convertIntegerListToByteArrayAndHashIt(n, engine, sigmai));
    }

    private byte[] verifyEntities() throws ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        NttPolynomial piNtt = sessionConfiguration.getClientsEphPubKey();
        NttPolynomial pjNtt = sessionConfiguration.getServersEphPubKey();
        byte[] ski = sessionConfiguration.getSharedSecret();
        // M1 = SHA3-256(pi || pj || ski) //
        byte[] m1 = Utils.concatenateTwoByteArraysAndHashThem(engine, piNtt.concatWith(pjNtt).toByteArray(), ski);
        // M2 = SHA3-256(pi || M1 || ski) //
        byte[] m2Prime = server.verifyEntities(m1);
        byte[] m2 = Utils.concatenateThreeByteArraysAndHash(engine, piNtt.toByteArray(), m1, ski);
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
        NttPolynomial aNtt = generateUniformPolyNtt(protocolConfiguration, randomCustomImple, publicSeedForA);
        // Generate salt.
        byte[] salt = new byte[SALTSIZE];
        randomCustomImple.getRandomBytes(salt);
        // Compute v.
        NttPolynomial vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
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
