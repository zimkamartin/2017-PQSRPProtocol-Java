package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import protocol.utils.Symmetric;

public class Main {

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT
    private static final String I = "identity123";
    private static final String PWD = "password123";
    private static final byte[] SALT = "salt123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT

    private static final Symmetric symmetric = new Symmetric.ShakeSymmetric();

    public static void main(String[] args) {
        Seeds seeds = createSeeds();
    }

    private static Seeds createSeeds() {
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd))
        String innerInput = I.concat(PWD);
        byte[] innerHash = new byte[32];
        symmetric.hash_h(innerHash, innerInput.getBytes(), 0);
        byte[] outerInput = new byte[SALT.length + innerHash.length];
        System.arraycopy(SALT, 0, outerInput, 0, SALT.length);
        System.arraycopy(innerHash, 0, outerInput, SALT.length, innerHash.length);
        byte[] seed1 = new byte[32];
        symmetric.hash_h(seed1, outerInput, 0);
        // seed2 = SHA3-256(seed1)
        byte[] seed2 = new byte[32];
        symmetric.hash_h(seed2, seed1, 0);

        return new Seeds(seed1, seed2);
    }
}