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
    private final int n;  // mozno n a q vytiahnut do maleho objektu aj s metodou porovnavania
    private final BigInteger q;
    private final ByteArrayWrapper publicSeedForA;
    private final NttImple ntt;
    private final Ding12Imple ding12;

    public ClientImple(RandomCustom random, Server server) {
        this.server = server;
        this.protocolConfiguration = server.getProtocolConfiguration();  // do buducnosti na toto zabudnem, iba si z toho vytiahnem n, q a etu. Do polynomov budem davat PolynomialConfig, ktory obsahuje n, q, zeta / zetaInverted
        this.n = this.protocolConfiguration.getN();
        this.q = this.protocolConfiguration.getQ();
        this.randomCustomImple = random;
        this.publicSeedForA = new ByteArrayWrapper(randomCustomImple, PUBLICSEEDFORASIZE);
        this.ntt = new NttImple(this.n, this.q);
        this.ding12 = new Ding12Imple(this.q);
    }

    private ByteArrayWrapper computeSeed1(ClientsKnowledge ck, ByteArrayWrapper salt) {
        ByteArrayWrapper identity = ck.getIdentity();
        ByteArrayWrapper password = ck.getPassword();
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd)) //
        return salt.concatWith(identity.concatWith(password).hashWrapped()).hashWrapped();
    }

    private NttPolynomial computeVNttFromANttAndSalt(ClientsKnowledge ck, NttPolynomial aNtt, ByteArrayWrapper salt) {
        // v = asv + 2ev //
        // Compute seeds.
        ByteArrayWrapper seed1 = computeSeed1(ck, salt);
        ByteArrayWrapper seed2 = seed1.hashWrapped();
        // Based on seeds (computed from private values) generate sv, ev.
        NttPolynomial svNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), seed1.defensiveCopy());
        NttPolynomial evNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), seed2.defensiveCopy());
        // Do all the math.
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, q);
        return multiply2NttTuplesAddThemTogetherNtt(aNtt.defensiveCopy(), svNtt, constantTwoPolyNtt, evNtt);
    }

    private SessionConfigurationClient computeSharedSecret(ClientsKnowledge ck) throws NotEnrolledClientException {
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
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        ServersResponseScs serversResponseScs = server.computeSharedSecret(ck.getIdentity(), piNtt.defensiveCopy());
        ByteArrayWrapper salt = serversResponseScs.getSalt();
        NttPolynomial pjNtt = serversResponseScs.getPjNtt();
        List<Integer> wj = serversResponseScs.getWj();
        // u = XOF(H(pi || pj)) //
        NttPolynomial uNtt = computeUNtt(protocolConfiguration, randomCustomImple, piNtt.defensiveCopy(), pjNtt.defensiveCopy());
        // v = asv + 2ev //
        NttPolynomial vNtt = computeVNttFromANttAndSalt(ck, aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        NttPolynomial e1DoublePrimeNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray());
        // Compute sv.
        NttPolynomial svNtt = generateRandomErrorPolyNtt(protocolConfiguration, randomCustomImple, ntt.getZetasArray(), computeSeed1(ck, salt));
        // Do all the math.
        NttPolynomial fstBracket = pjNtt.subtract(vNtt);
        NttPolynomial sndBracket = svNtt.add(s1Ntt);
        ClassicalPolynomial ki = multiply3NttTuplesAndAddThemTogether(fstBracket, sndBracket, uNtt, vNtt, constantTwoPolyNtt, e1DoublePrimeNtt, ntt.getZetasInvertedArray());
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> ding12.robustExtractor(ki.getCoeffs().get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        //System.out.println(sigmai);
        ByteArrayWrapper ski = new ByteArrayWrapper(Utils.convertIntegerListToByteArray(sigmai)).hashWrapped();  // TODO zmazat tuto utilku, dat to ako konstruktor do ByteArrayWrapper
        return new SessionConfigurationClient(piNtt.defensiveCopy(), pjNtt.defensiveCopy(), ski, serversResponseScs.getScs());
    }

    private ByteArrayWrapper verifyEntities(SessionConfigurationClient scs) throws ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        NttPolynomial piNtt = scs.getClientsEphPubKey();
        NttPolynomial pjNtt = scs.getServersEphPubKey();
        ByteArrayWrapper ski = scs.getSharedSecret();
        // M1 = SHA3-256(pi || pj || ski) //
        ByteArrayWrapper m1 = piNtt.concatWith(pjNtt).toByteArrayWrapper().concatWith(ski).hashWrapped();
        // M2 = SHA3-256(pi || M1 || ski) //
        ByteArrayWrapper m2Prime = server.verifyEntities(scs.getServersSessionConfiguration(), m1.defensiveCopy());
        ByteArrayWrapper m2 = piNtt.toByteArrayWrapper().concatWith(m1).concatWith(ski).hashWrapped();
        // VERIFY that M2 == M2'. If true, return key.
        if (!m2.equals(m2Prime)) {
            throw new ServerNotAuthenticatedException("M2 does not equal to M2'.");
        }
        return ski;
    }

    public void enroll(ClientsKnowledge ck) {
        // PHASE 0 //
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        NttPolynomial aNtt = generateUniformPolyNtt(protocolConfiguration, randomCustomImple, publicSeedForA);
        // Generate salt.
        ByteArrayWrapper salt = new ByteArrayWrapper(randomCustomImple, SALTSIZE);
        // Compute v.
        NttPolynomial vNtt = computeVNttFromANttAndSalt(ck, aNtt, salt);
        // Send public seed for a, identity, salt and v in NTT form to the server. //
        server.enrollClient(publicSeedForA, ck.getIdentity(), salt, vNtt);
    }

    public ByteArrayWrapper login(ClientsKnowledge ck) throws NotEnrolledClientException, ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        // PHASE 1 //
        SessionConfigurationClient scc = computeSharedSecret(ck);
        // PHASE 2 //
        return verifyEntities(scc);
        // FOR THE FUTURE: Pripadne vratit loginResponse
    }
}
