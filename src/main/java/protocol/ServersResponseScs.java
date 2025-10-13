package protocol;

import protocol.polynomial.NttPolynomial;
import protocol.server.SessionConfigurationServer;

import java.util.List;

/**
 * The {@code ServersResponseScs} class represents server's response during server - client communication
 * when computing shared secret key.
 *
 * <p>It consists of the following attributes:</p>
 * <ul>
 *     <li>{@code salt}  – {@code ByteArrayWrapper}, client's salt</li>
 *     <li>{@code pjNtt} – {@code NttPolynomial}, server's ephemeral public key in NTT form</li>
 *     <li>{@code wj}    – {@code List<Integer>}, list of integers resulting from applying Signal function to polynomial kj
 *                         (for more information see <a href="https://doi.org/10.1007/978-3-319-75160-3_8">protocol</a>
 *                         definition)</li>
 *     <li>{@code scs}   - {@code SessionConfigurationServer}, server's session configuration</li>
 * </ul>
 *
 * @author Martin Zimka
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
        this.wj = List.copyOf(wj);  // make sure that it will not be changed after created
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
