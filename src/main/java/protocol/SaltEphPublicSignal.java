package protocol;

import protocol.polynomial.Polynomial;

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
    private final Polynomial pjNtt;
    /**
     * List of integers resulting from applying Signal function to polynomial kj.
     */
    private final List<Integer> wj;

    public SaltEphPublicSignal(byte[] salt, Polynomial pjNtt, List<Integer> wj) {
        this.salt = salt;
        this.pjNtt = pjNtt.defensiveCopy();
        this.wj = List.copyOf(wj);
    }

    public byte[] getSalt() {
        return salt;
    }

    public Polynomial getPjNtt() {
        return pjNtt.defensiveCopy();
    }

    public List<Integer> getWj() {
        return List.copyOf(wj);
    }
}
