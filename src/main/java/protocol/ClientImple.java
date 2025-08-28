package protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
    // DATABASE //
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
        List<BigInteger> aSvNtt = ntt.multiplyNttPolys(aNtt, svNtt);
        List<BigInteger> twoEvNtt = ntt.multiplyNttPolys(ntt.generateConstantTwoPolynomialNtt(), evNtt);
        return ntt.addPolys(aSvNtt, twoEvNtt);
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
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Compute s1.
        List<BigInteger> s1Ntt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Compute e1.
        List<BigInteger> e1Ntt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Do all the math.
        List<BigInteger> aS1Ntt = ntt.multiplyNttPolys(aNtt, s1Ntt);
        List<BigInteger> twoE1Ntt = ntt.multiplyNttPolys(constantTwoPolyNtt, e1Ntt);
        this.piNtt = ntt.addPolys(aS1Ntt, twoE1Ntt);
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(cs.getIdentity(), this.piNtt);
        byte[] salt = sPjNttWj.getSalt();
        this.pjNtt = sPjNttWj.getPjNtt();
        List<Integer> wj = sPjNttWj.getWj();
        // u = XOF(H(pi || pj)) //
        byte[] hash = new byte[32];
        engine.hash(hash, Utils.concatBigIntegerListsToByteArray(this.piNtt, this.pjNtt));
        List<BigInteger> uNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, uNtt, hash);
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
        List<BigInteger> fstMultiNtt = ntt.multiplyNttPolys(fstBracket, sndBracket);
        List<BigInteger> sndMultiNtt = ntt.multiplyNttPolys(uNtt, vNtt);
        List<BigInteger> trdMultiNtt = ntt.multiplyNttPolys(constantTwoPolyNtt, e1DoublePrimeNtt);
        List<BigInteger> addedFstTwoNtt = ntt.addPolys(fstMultiNtt, sndMultiNtt);
        List<BigInteger> kiNtt = ntt.addPolys(addedFstTwoNtt, trdMultiNtt);
        List<BigInteger> ki = ntt.convertFromNtt(kiNtt);
        // sigmai = Mod_2(ki, wj) //
        List<Integer> sigmai = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(ki.get(i), wj.get(i))).toList();
        // ski = SHA3-256(sigmai) //
        byte[] ski = new byte[32];
        byte[] sigmaiByteArray = new byte[n];
        for (int i = 0; i < n; i++) {
            sigmaiByteArray[i] = sigmai.get(i).byteValue();
        }
        engine.hash(ski, sigmaiByteArray);
        this.ski = ski;
    }

    public byte[] verifyEntities() {
        // M1 = SHA3-256(pi || pj || ski) //
        byte[] m1 = Utils.concatenateTwoByteArraysAndHash(engine, Utils.concatBigIntegerListsToByteArray(this.piNtt, this.pjNtt), this.ski);
        // M2 = SHA3-256(pi || M1 || ski) //
        byte[] m2Prime = server.verifyEntities(m1);
        byte[] m2 = Utils.concatenateThreeByteArraysAndHash(engine, Utils.convertBigIntegerListToByteArray(piNtt), m1, this.ski);
        // TODO: Verify matches and if all OK, return key.
        return new byte[0];
    }
}
