package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;

public class Main {

    public static void main(String[] args) {
        createSeeds();
    }

    private static void createSeeds() {
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd))
        final SHA3Digest digest = new SHA3Digest(256);
        digest.update("".getBytes(), 0, 0);
        byte[] output = new byte[32];
        digest.doFinal(output, 0);
        // seed2 = SHA3-256(seed1)
    }
}