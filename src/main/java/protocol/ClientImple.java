package protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static protocol.Utils.computeUNtt;
import static protocol.Utils.convertBigIntegerListToByteArray;

public class ClientImple {

    private static final int PUBLICSEEDFORASIZE = 34;  // Size could be changed however you wish.
    private static final int SALTSIZE = 34;  // Size could be changed however you wish.

    private final Server server;
    private final PublicParams publicParams;
    private final int n;
    private final BigInteger q;
    private final int eta;
    private final byte[] publicSeedForA = new byte[PUBLICSEEDFORASIZE];
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;
    private final Ntt ntt;
    private final Magic magic;
    // TODO figure out what to do with these 3 vars //
    private byte[] ski = null;
    private List<BigInteger> piNtt = null;
    private List<BigInteger> pjNtt = null;

    public ClientImple(Server server) {
        this.server = server;
        this.publicParams = server.getPublicParams();
        this.n = this.publicParams.getN();
        this.q = this.publicParams.getQ();
        this.eta = this.publicParams.getEta();
        this.engine.getRandomBytes(this.publicSeedForA);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
        this.magic = new MagicImple(this.q);
    }

    private byte[] computeSeed1(ClientsSecrets cs, byte[] salt) {
        byte[] identity = cs.getIdentity();
        byte[] password = cs.getPassword();
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd)) //
        byte[] innerHash = Utils.concatenateTwoByteArraysAndHash(engine, identity, password);
        return Utils.concatenateTwoByteArraysAndHash(engine, salt, innerHash);
    }

    private List<BigInteger> computeVNttFromANttAndSalt(ClientsSecrets cs, List<BigInteger> aNtt, byte[] salt) {
        // v = asv + 2ev //
        // Compute seeds.
        byte[] seed1 = computeSeed1(cs, salt);
        byte[] seed2 = new byte[32];
        engine.hash(seed2, seed1);
        // Based on seeds (computed from private values) generate sv, ev.
        List<BigInteger> sv = new ArrayList<>(n);
        List<BigInteger> ev = new ArrayList<>(n);
        Utils.getEtaNoise(publicParams, mlkem, engine, sv, seed1);
        Utils.getEtaNoise(publicParams, mlkem, engine, ev, seed2);
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        List<BigInteger> evNtt = ntt.convertFromNtt(ev);
        // Do all the math.
        return Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, svNtt, ntt.generateConstantTwoPolynomialNtt(), evNtt);
    }

    public void enrollClient(ClientsSecrets cs) {
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Generate salt.
        byte[] salt = new byte[SALTSIZE];
        engine.getRandomBytes(salt);
        // Compute v.
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
        // Send public seed for a, identity, salt and v in NTT form to the server. //
        server.enrollClient(publicSeedForA, cs.getIdentity(), salt, vNtt);
    }

    public void computeSharedSecret(ClientsSecrets cs) {
        List<BigInteger> constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // pi = as1 + 2e1 //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(n);
        // Compute s1.
        List<BigInteger> s1Ntt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Compute e1.
        List<BigInteger> e1Ntt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Do all the math.
        this.piNtt = Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, s1Ntt, constantTwoPolyNtt, e1Ntt);
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(cs.getIdentity(), this.piNtt);
        byte[] salt = sPjNttWj.getSalt();
        this.pjNtt = sPjNttWj.getPjNtt();
        List<Integer> wj = sPjNttWj.getWj();
        // u = XOF(H(pi || pj)) //
        List<BigInteger> uNtt = computeUNtt(engine, mlkem, n, piNtt, pjNtt);
        // v = asv + 2ev //
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(cs, aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        List<BigInteger> e1DoublePrimeNtt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Compute sv.
        List<BigInteger> sv = new ArrayList<>(n);
        Utils.getEtaNoise(publicParams, mlkem, engine, sv, computeSeed1(cs, salt));
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        // Do all the math.
        List<BigInteger> fstBracket = ntt.subtractPolys(this.pjNtt, vNtt);
        List<BigInteger> sndBracket = ntt.addPolys(svNtt, s1Ntt);
        List<BigInteger> ki = Utils.multiply3NttTuplesAndAddThemTogether(ntt, fstBracket, sndBracket, uNtt, vNtt, constantTwoPolyNtt, e1DoublePrimeNtt);
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(ki.get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        this.ski = Utils.hashConvertIntegerListToByteArray(n, engine, sigmai);
    }

    public byte[] verifyEntities() {
        // M1 = SHA3-256(pi || pj || ski) //
        byte[] m1 = Utils.concatenateTwoByteArraysAndHash(engine, Utils.concatBigIntegerListsToByteArray(this.piNtt, this.pjNtt), this.ski);
        // M2 = SHA3-256(pi || M1 || ski) //
        byte[] m2Prime = server.verifyEntities(m1);
        byte[] m2 = Utils.concatenateThreeByteArraysAndHash(engine, Utils.convertBigIntegerListToByteArray(piNtt), m1, this.ski);
        // VERIFY that M2' == M2. If true, return key.
        ByteArrayWrapper m2PrimeWrapped = new ByteArrayWrapper(m2Prime);
        ByteArrayWrapper m2Wrapped = new ByteArrayWrapper(m2Prime);
        return (m2PrimeWrapped.equals(m2Wrapped)) ? this.ski : null;
    }
}
