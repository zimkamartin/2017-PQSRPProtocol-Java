package protocol;

import protocol.polynomial.NttPolynomial;
import protocol.server.SessionConfigurationServer;

import java.util.List;

/**
 * Represents response from the server during server - client communication
 * when computing shared secret key.
 */
public class ServersResponseScs {

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

    private final SessionConfigurationServer scs;

    public ServersResponseScs(ByteArrayWrapper salt, NttPolynomial pjNtt, List<Integer> wj, SessionConfigurationServer scs) {
        this.salt = salt;
        this.pjNtt = pjNtt;
        this.wj = List.copyOf(wj);
        this.scs = scs;
    }

    public ByteArrayWrapper getSalt() {
        return salt;
    }

    public NttPolynomial getPjNtt() {
        return pjNtt;
    }

    public List<Integer> getWj() {
        return wj;
    }

    public SessionConfigurationServer getScs() {
        return scs;
    }
}
