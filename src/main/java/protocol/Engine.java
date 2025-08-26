package protocol;

/**
 * Represents engine of the protocol, so all functions that must have some internal state during the run of the protocol.
 * <p>
 * Ok, must is maybe too strong - we do not want to create repeatedly new instances of SecureRandom(), ...,
 * so we are using this solution.
 * Represents XOF function - SHAKE128, hash function - SHA3-256, and random function - SecureRandom().
 * Heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/Symmetric.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMEngine.java
 * </p>
 */
public interface Engine {

    /**
     * @return the output block size of the underlying XOF (SHAKE128), in bytes
     */
    int getXofBlockBytes();

    /**
     * @param seed - used for seeding XOF (SHAKE128)
     */
    void xofAbsorb(byte[] seed);

    /**
     * @param out - byte array where hash will be squeezed to
     * @param outOffset - position from which hash will be squeezed to
     * @param outLen - how many bytes to squeeze
     */
    void xofSqueezeBlocks(byte[] out, int outOffset, int outLen);

    /**
     * @param out - byte array where hashed will be put
     * @param in - input for the hash function
     */
    void hash(byte[] out, byte[] in);

    /**
     * @param out - byte array where the output from pseudo-random function will be put
     * @param seed - seed for the pseudo-random function
     */
    void prf(byte[] out, byte[] seed);

    /**
     * @param buf - byte array which will be filed by random bytes
     */
    public void getRandomBytes(byte[] buf);

    /**
     * @return random bit
     */
    public int getRandomBit();
}