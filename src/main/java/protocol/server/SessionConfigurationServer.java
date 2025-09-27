package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

/**
 * The {@code SessionConfigurationServer} class represents the server’s perspective of a client–server session.
 *
 * <p>The session configuration consists of:</p>
 * <ul>
 *   <li>{@code piNtt} – {@code NttPolynomial}, the NTT (Number Theoretic Transform) representation of the client’s
 *                                              ephemeral public key</li>
 *   <li>{@code pjNtt} – {@code NttPolynomial}, the NTT representation of the server’s ephemeral public key</li>
 *   <li>{@code skj}   – {@code ByteArrayWrapper}, the shared secret computed on the server’s side</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class SessionConfigurationServer {

    private final NttPolynomial piNtt;
    private final NttPolynomial pjNtt;
    private final ByteArrayWrapper skj;

    SessionConfigurationServer(NttPolynomial piNtt, NttPolynomial pjNtt, ByteArrayWrapper skj) {
        this.piNtt = piNtt;
        this.pjNtt = pjNtt;
        this.skj = skj;
    }

    NttPolynomial getClientsEphPubKey() {
        return piNtt;
    }

    NttPolynomial getServersEphPubKey() {
        return pjNtt;
    }

    ByteArrayWrapper getSharedSecret() {
        return skj;
    }
}
