package protocol;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    public ClientImple(Server server) {
        this.server = server;
        PublicParams publicParams = server.getPublicParams();
        this.n = publicParams.getN();
        this.q = publicParams.getQ();
        this.eta = publicParams.getEta();
        this.engine.getRandomBytes(publicSeedForA);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
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

    public void enrollClient() {
        // v = asv + 2ev //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(n);
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Generate salt and compute seeds.
        byte[] salt = new byte[SALTSIZE];
        engine.getRandomBytes(salt);
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
        List<BigInteger> vNtt = ntt.addPolys(aSvNtt, twoEvNtt);
        // Send it to the server //
        server.enrollClient(publicSeedForA, I, salt, vNtt);
    }

    public void computeSharedSecret() {
        // Compute pi
        // SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(I, pi);
        // Compute u
        // ...
        // Compute Shared secret - save it as an attribute of this Class  - is it the best way? Where to save secret? Or return it in some way?
    }

    public void verifyEntities() {
        // Compute M1
        // byte[] m2Prime = server.verifyEntities(m1);
        // Compute M2
    }
}
