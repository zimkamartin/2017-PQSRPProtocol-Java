package protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ClientImple {

    private static final int PUBLICSEEDFORASIZE = 34;  // Size could be changed however you wish.
    private static final int SALTSIZE = 34;  // Size could be changed however you wish.

    private final Server server;
    private final int n;
    private final BigInteger q;
    private final int eta;
    private final byte[] publicSeedForA = new byte[PUBLICSEEDFORASIZE];
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;
    private final Ntt ntt;
    private final Magic magic;
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    // THIS IS NOT HOW TO DO IT ! THIS IS JUST FOR PROOF-OF-CONCEPT ! THIS IS NOT HOW TO DO IT !
    // DATABASE //
    private byte[] ski = null;
    // THIS IS NOT HOW TO DO IT ! THIS IS JUST FOR PROOF-OF-CONCEPT ! THIS IS NOT HOW TO DO IT !

    public ClientImple(Server server) {
        this.server = server;
        PublicParams publicParams = server.getPublicParams();
        this.n = publicParams.getN();
        this.q = publicParams.getQ();
        this.eta = publicParams.getEta();
        this.engine.getRandomBytes(publicSeedForA);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
        this.magic = new MagicImple(this.q);
    }

    private byte[] computeSeed1(byte[] salt) {
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd)) //
        byte[] innerInput = new byte[I.length + PWD.length];
        System.arraycopy(I, 0, innerInput, 0, I.length);
        System.arraycopy(PWD, 0, innerInput, I.length, PWD.length);
        byte[] innerHash = new byte[32];
        engine.hash(innerHash, innerInput);
        byte[] outerInput = new byte[salt.length + innerHash.length];
        System.arraycopy(salt, 0, outerInput, 0, salt.length);
        System.arraycopy(innerHash, 0, outerInput, salt.length, innerHash.length);
        byte[] seed1 = new byte[32];
        engine.hash(seed1, outerInput);
        return seed1;
    }

    private void getEtaNoise(List<BigInteger> r, byte[] seed) {
        byte[] buf = new byte[n * eta / 4];
        engine.prf(buf, seed);
        mlkem.generateCbdPolynomial(r, buf, eta);
    }

    private List<BigInteger> generateRandomErrorPolyNtt() {
        List<BigInteger> e = new ArrayList<>(n);
        byte[] eRandomSeed = new byte[34];
        engine.getRandomBytes(eRandomSeed);
        getEtaNoise(e, eRandomSeed);
        return ntt.convertFromNtt(e);
    }

    private List<BigInteger> computeVNttFromANttAndSalt(List<BigInteger> aNtt, byte[] salt) {
        // v = asv + 2ev //
        // Compute seeds.
        byte[] seed1 = computeSeed1(salt);
        byte[] seed2 = new byte[32];
        engine.hash(seed2, seed1);
        // Based on seeds (computed from private values) generate sv, ev.
        List<BigInteger> sv = new ArrayList<>(n);
        List<BigInteger> ev = new ArrayList<>(n);
        getEtaNoise(sv, seed1);
        getEtaNoise(ev, seed2);
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        List<BigInteger> evNtt = ntt.convertFromNtt(ev);
        // Do all the math.
        List<BigInteger> aSvNtt = ntt.multiplyNttPolys(aNtt, svNtt);
        List<BigInteger> twoEvNtt = ntt.multiplyNttPolys(ntt.generateConstantTwoPolynomialNtt(), evNtt);
        return ntt.addPolys(aSvNtt, twoEvNtt);
    }

    public void enrollClient() {
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Generate salt.
        byte[] salt = new byte[SALTSIZE];
        engine.getRandomBytes(salt);
        // Compute v.
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(aNtt, salt);
        // Send public seed for a, identity, salt and v in NTT form to the server. //
        server.enrollClient(publicSeedForA.clone(), I.clone(), salt, List.copyOf(vNtt));  // salt will be forgotten, no need for copy
    }

    private static byte[] concatBigIntegerListsToByteArray(List<BigInteger> a, List<BigInteger> b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (BigInteger coeff : a) {
                out.write(coeff.toByteArray());
            }
            for (BigInteger coeff : b) {
                out.write(coeff.toByteArray());
            }
        } catch (IOException e) {
            System.out.println("This should not have happened.");
        }
        return out.toByteArray();
    }

    public void computeSharedSecret() {
        List<BigInteger> constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // pi = as1 + 2e1 //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Compute s1.
        List<BigInteger> s1Ntt = generateRandomErrorPolyNtt();
        // Compute e1.
        List<BigInteger> e1Ntt = generateRandomErrorPolyNtt();
        // Do all the math.
        List<BigInteger> aS1Ntt = ntt.multiplyNttPolys(aNtt, s1Ntt);
        List<BigInteger> twoE1Ntt = ntt.multiplyNttPolys(constantTwoPolyNtt, e1Ntt);
        List<BigInteger> piNtt = ntt.addPolys(aS1Ntt, twoE1Ntt);
        // Send identity and ephemeral public key pi in NTT form to the server. //
        // Receive salt, ephemeral public key pj in NTT form and wj. //
        SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(I.clone(), List.copyOf(piNtt));
        byte[] salt = sPjNttWj.getSalt();
        List<BigInteger> pjNtt = sPjNttWj.getPjNtt();
        List<Integer> wj = sPjNttWj.getWj();
        // u = XOF(H(pi || pj)) //
        byte[] hash = new byte[32];
        engine.hash(hash, concatBigIntegerListsToByteArray(piNtt, pjNtt));
        List<BigInteger> uNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, uNtt, hash);
        // v = asv + 2ev //
        List<BigInteger> vNtt = computeVNttFromANttAndSalt(aNtt, salt);
        // ki = (pj − v)(sv + s1) + uv + 2e1'' //
        // Compute e1''.
        List<BigInteger> e1DoublePrimeNtt = generateRandomErrorPolyNtt();
        // Compute sv.
        List<BigInteger> sv = new ArrayList<>(n);
        getEtaNoise(sv, computeSeed1(salt));
        List<BigInteger> svNtt = ntt.convertToNtt(sv);
        // Do all the math.
        List<BigInteger> fstBracket = ntt.subtractPolys(pjNtt, vNtt);
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

    public void verifyEntities() {
        // Compute M1
        // byte[] m2Prime = server.verifyEntities(m1);
        // Compute M2
    }
}
