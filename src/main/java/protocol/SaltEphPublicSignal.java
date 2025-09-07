package protocol;

import protocol.polynomial.NttPolynomial;

import java.util.List;

/**
 * Represents response from the server during server - client communication
 * when computing shared secret key.
 */
public class SaltEphPublicSignal {

    /**
     * Used on client's side when computing seed1.
     */
    private final ByteArrayWrapper salt;
    /**
     * Polynomial representing server's ephemeral public key in NTT form.
     */
    private final NttPolynomial pjNtt;
    /**
     * List of integers resulting from applying Signal function to polynomial kj.
     */
    private final List<Integer> wj;

    public SaltEphPublicSignal(ByteArrayWrapper salt, NttPolynomial pjNtt, List<Integer> wj) {
        this.salt = salt.defensiveCopy();
        this.pjNtt = pjNtt.defensiveCopy();
        this.wj = List.copyOf(wj);
    }

    public ByteArrayWrapper getSalt() {
        return salt.defensiveCopy();
    }

    public NttPolynomial getPjNtt() {
        return pjNtt.defensiveCopy();
    }

    public List<Integer> getWj() {
        return List.copyOf(wj);
    }
}
