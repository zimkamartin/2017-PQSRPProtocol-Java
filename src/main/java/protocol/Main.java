package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;

public class Main {

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT
    private static final String I = "identity123";
    private static final String PWD = "password123";
    private static final byte[] SALT = "salt123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT

    private static final SHA3Digest sha3Digest256 = new SHA3Digest(256);

    public static void main(String[] args) {
        Seeds seeds = createSeeds();
    }

    private static Seeds createSeeds() {
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd))
        String innerInput = I.concat(PWD);
        sha3Digest256.reset();
        sha3Digest256.update(innerInput.getBytes(), 0, innerInput.length());
        byte[] innerHash = new byte[32];
        sha3Digest256.doFinal(innerHash, 0);
        byte[] outerInput = new byte[SALT.length + innerHash.length];
        System.arraycopy(SALT, 0, outerInput, 0, SALT.length);
        System.arraycopy(innerHash, 0, outerInput, SALT.length, innerHash.length);
        sha3Digest256.reset();
        sha3Digest256.update(outerInput, 0, outerInput.length);
        byte[] seed1 = new byte[32];
        sha3Digest256.doFinal(seed1, 0);
        // seed2 = SHA3-256(seed1)
        sha3Digest256.reset();
        sha3Digest256.update(seed1, 0, seed1.length);
        byte[] seed2 = new byte[32];
        sha3Digest256.doFinal(seed2, 0);

        return new Seeds(seed1, seed2);
    }
}