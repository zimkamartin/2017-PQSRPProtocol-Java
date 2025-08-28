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
     * Polynomial representing server's ephemeral public key in NTT form.
     */
    private final List<BigInteger> pjNtt;
    /**
     * Polynomial where coefficients are result of applying Signal function.
     */
    private final List<Integer> wj;

    public SaltEphPublicSignal(byte[] salt, List<BigInteger> pjNtt, List<Integer> wj) {
        this.salt = salt;
        this.pjNtt = pjNtt;
        this.wj = wj;
    }

    public byte[] getSalt() {
        return salt;
    }

    public List<BigInteger> getPjNtt() {
        return pjNtt;
    }

    public List<Integer> getWj() {
        return wj;
    }
}
