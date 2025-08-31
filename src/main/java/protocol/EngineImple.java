package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import java.util.Random;

/**
 * Represents engine of the protocol, so all functions that must have some internal state during the run of the protocol.
 * <p>
 * Ok, must is maybe too strong - we do not want to create repeatedly new instances of SecureRandom(), ..., so we are using this solution.
 * Represents XOF function - SHAKE128, hash function - SHA3-256, and random function - SecureRandom().
 * Heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/Symmetric.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMEngine.java
 * </p>
 */
public class EngineImple {
    private static final int XOFBLOCKBYTES = 168;
    private static final SHAKEDigest xof = new SHAKEDigest(128);
    private static final SHA3Digest sha3Digest256 = new SHA3Digest(256);
    private static final SHAKEDigest shakeDigest = new SHAKEDigest(256);
    private final Random random;

    public EngineImple(Random random) {
        this.random = random;
    }

    /**
     * @return the output block size of the underlying XOF (SHAKE128), in bytes
     */
    public int getXofBlockBytes() {
        return XOFBLOCKBYTES;
    }

    /**
     * @param seed - used for seeding XOF (SHAKE128)
     */
    public void xofAbsorb(byte[] seed) {
        xof.reset();
        xof.update(seed, 0, seed.length);
    }

    /**
     * @param out - byte array where hash will be squeezed to
     * @param outOffset - position from which hash will be squeezed to
     * @param outLen - how many bytes to squeeze
     */
    public void xofSqueezeBlocks(byte[] out, int outOffset, int outLen) {
        xof.doOutput(out, outOffset, outLen);
    }

    /**
     * @param out - byte array where hashed will be put
     * @param in - input for the hash function (SHA3-256)
     */
    public void hash(byte[] out, byte[] in) {
        sha3Digest256.update(in, 0, in.length);
        sha3Digest256.doFinal(out, 0);
    }

    /**
     * @param out - byte array where the output from pseudo-random function will be put
     * @param seed - seed for the pseudo-random function (SHAKE256)
     */
    public void prf(byte[] out, byte[] seed) {
        shakeDigest.update(seed, 0, seed.length);
        shakeDigest.doFinal(out, 0, out.length);
    }

    /**
     * @param buf - byte array which will be filed by random bytes
     */
    public void getRandomBytes(byte[] buf) {
        random.nextBytes(buf);
    }

    /**
     * @return random bit
     */
    public int getRandomBit() {
        return random.nextInt(2);
    }
}