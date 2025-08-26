package protocol;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents response from the server during server - client communication
 * when computing shared secret key.
 */
public class SaltEphPublicSignal {

    /**
     * Used on client's side when computing seed1.
     */
    private final byte[] salt;
    /**
     * Polynomial representing ephemeral public key used on both sides.
     */
    private final List<BigInteger> pj;
    /**
     * Polynomial where coefficients are result of applying Signal function.
     */
    private final List<BigInteger> wj;

    public SaltEphPublicSignal(byte[] salt, List<BigInteger> pj, List<BigInteger> wj) {
        this.salt = salt;
        this.pj = pj;
        this.wj = wj;
    }

    public byte[] getSalt() {
        return salt;
    }

    public List<BigInteger> getPj() {
        return pj;
    }

    public List<BigInteger> getWj() {
        return wj;
    }
}
