package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import java.security.SecureRandom;

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
public class EngineImple implements Engine {
    private static final int xofBlockBytes = 168;
    private final SHAKEDigest xof = new SHAKEDigest(128);
    private final SHA3Digest sha3Digest256 = new SHA3Digest(256);
    private final SHAKEDigest shakeDigest = new SHAKEDigest(256);
    private final SecureRandom random = new SecureRandom();

    public EngineImple() {}

    @Override
    public int getXofBlockBytes() {
        return xofBlockBytes;
    }

    @Override
    public void xofAbsorb(byte[] seed) {
        xof.reset();
        xof.update(seed, 0, seed.length);
    }

    @Override
    public void xofSqueezeBlocks(byte[] out, int outOffset, int outLen) {
        xof.doOutput(out, outOffset, outLen);
    }

    @Override
    public void hash(byte[] out, byte[] in) {
        sha3Digest256.update(in, 0, in.length);
        sha3Digest256.doFinal(out, 0);
    }

    @Override
    public void prf(byte[] out, byte[] seed) {
        shakeDigest.update(seed, 0, seed.length);
        shakeDigest.doFinal(out, 0, out.length);
    }

    @Override
    public void getRandomBytes(byte[] buf) {
        this.random.nextBytes(buf);
    }

    @Override
    public int getRandomBit() {
        return this.random.nextInt(2);
    }
}