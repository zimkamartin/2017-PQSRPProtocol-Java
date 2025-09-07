package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import protocol.random.RandomCustom;

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
    private final SHA3Digest sha3Digest256 = new SHA3Digest(256);  // defined in protocol

    public EngineImple() {}

    /**
     * @param out - byte array where hashed will be put
     * @param in - input for the hash function (SHA3-256)
     */
    public void hash(byte[] out, byte[] in) {
        sha3Digest256.update(in, 0, in.length);
        sha3Digest256.doFinal(out, 0);
    }
}