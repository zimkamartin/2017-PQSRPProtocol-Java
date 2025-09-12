package protocol.client;

import protocol.*;
import protocol.polynomial.ClassicalPolynomial;
import protocol.polynomial.NttPolynomial;
import protocol.polynomial.PolynomialConfig;
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
    private final RandomCustom randomCustomImple;
    private final int n;
    private final ByteArrayWrapper publicSeedForA;
    private final Ding12Imple ding12;
    private final PolynomialConfig polynomialConfig;

    public ClientImple(RandomCustom random, Server server) {
        this.server = server;
        ProtocolConfiguration protocolConfiguration = server.getProtocolConfiguration();
        this.n = protocolConfiguration.getN();
        BigInteger q = protocolConfiguration.getQ();
        this.randomCustomImple = random;
        this.publicSeedForA = new ByteArrayWrapper(randomCustomImple, PUBLICSEEDFORASIZE);
        this.polynomialConfig = new PolynomialConfig(this.n, q);
        this.ding12 = new Ding12Imple(q);
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
        NttPolynomial svNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple, seed1);
        NttPolynomial evNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple, seed2);
        // Do all the math.
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, polynomialConfig);
        return multiply2NttTuplesAddThemTogetherNtt(aNtt, svNtt, constantTwoPolyNtt, evNtt);
    }

    private SessionConfigurationClient computeSharedSecret(ClientsKnowledge ck) {
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, polynomialConfig);
        // pi = as1 + 2e1 //
        // Create polynomial a from public seed.
        NttPolynomial aNtt = generateUniformPolyNtt(polynomialConfig, randomCustomImple, publicSeedForA);
        // Compute s1.
        NttPolynomial s1Ntt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Compute e1.
        NttPolynomial e1Ntt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Do all the math.
        NttPolynomial piNtt = multiply2NttTuplesAddThemTogetherNtt(aNtt, s1Ntt, constantTwoPolyNtt, e1Ntt);
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        ServersResponseScs serversResponseScs = server.computeSharedSecret(ck.getIdentity(), piNtt);
        if (serversResponseScs == null) {
            return null;
        }
        ByteArrayWrapper salt = serversResponseScs.getSalt();
        NttPolynomial pjNtt = serversResponseScs.getPjNtt();
        List<Integer> wj = serversResponseScs.getWj();
        // u = XOF(H(pi || pj)) //
        NttPolynomial uNtt = computeUNtt(polynomialConfig, randomCustomImple, piNtt, pjNtt);
        // v = asv + 2ev //
        NttPolynomial vNtt = computeVNttFromANttAndSalt(ck, aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        NttPolynomial e1DoublePrimeNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Compute sv.
        NttPolynomial svNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple, computeSeed1(ck, salt));
        // Do all the math.
        NttPolynomial fstBracket = pjNtt.subtract(vNtt);
        NttPolynomial sndBracket = svNtt.add(s1Ntt);
        ClassicalPolynomial ki = multiply3NttTuplesAndAddThemTogether(polynomialConfig, fstBracket, sndBracket, uNtt, vNtt, constantTwoPolyNtt, e1DoublePrimeNtt);
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> ding12.robustExtractor(ki.getCoeffs().get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        //System.out.println(sigmai);
        ByteArrayWrapper ski = new ByteArrayWrapper(sigmai).hashWrapped();
        return new SessionConfigurationClient(piNtt, pjNtt, ski, serversResponseScs.getScs());
    }

    private ByteArrayWrapper verifyEntities(SessionConfigurationClient scs) {
        NttPolynomial piNtt = scs.getClientsEphPubKey();
        NttPolynomial pjNtt = scs.getServersEphPubKey();
        ByteArrayWrapper ski = scs.getSharedSecret();
        // M1 = SHA3-256(pi || pj || ski) //
        ByteArrayWrapper m1 = piNtt.concatWith(pjNtt).toByteArrayWrapper().concatWith(ski).hashWrapped();
        // M2 = SHA3-256(pi || M1 || ski) //
        ByteArrayWrapper m2Prime = server.verifyEntities(scs.getServersSessionConfiguration(), m1);
        ByteArrayWrapper m2 = piNtt.toByteArrayWrapper().concatWith(m1).concatWith(ski).hashWrapped();
        // VERIFY that M2 == M2'. If true, return key.
        return m2.equals(m2Prime) ? ski : null;
    }

    public void enroll(ClientsKnowledge ck) {
        // PHASE 0 //
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        NttPolynomial aNtt = generateUniformPolyNtt(polynomialConfig, randomCustomImple, publicSeedForA);
        // Generate salt.
        ByteArrayWrapper salt = new ByteArrayWrapper(randomCustomImple, SALTSIZE);
        // Compute v.
        NttPolynomial vNtt = computeVNttFromANttAndSalt(ck, aNtt, salt);
        // Send public seed for a, identity, salt and v in NTT form to the server. //
        server.enrollClient(publicSeedForA, ck.getIdentity(), salt, vNtt);
    }

    public ByteArrayWrapper login(ClientsKnowledge ck) {
        // PHASE 1 //
        SessionConfigurationClient scc = computeSharedSecret(ck);
        if (scc == null) {
            return null;
        }
        // PHASE 2 //
        return verifyEntities(scc);
        // FOR THE FUTURE pre klienta: Pripadne vratit loginResponse: kluc = byteArrayWrapper[], loginOk = boolin (aby to nebolo iba cez kluc = null)
    }
}
