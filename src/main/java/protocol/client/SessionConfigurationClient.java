package protocol.client;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;
import protocol.server.SessionConfigurationServer;

/**
 * The {@code SessionConfigurationClient} class represents the client’s perspective of a client–server session.
 *
 * <p>The session configuration consists of:</p>
 * <ul>
 *   <li>{@code piNtt} – {@code NttPolynomial}, the NTT representation of the client’s ephemeral public key</li>
 *   <li>{@code pjNtt} – {@code NttPolynomial}, the NTT representation of the server’s ephemeral public key</li>
 *   <li>{@code ski}   – {@code ByteArrayWrapper}, the shared secret computed on the client’s side</li>
 *   <li>{@code scs}   – {@code SessionConfigurationServer}, the server’s corresponding session configuration</li>
 * </ul>
 *
 * @author Martin Zimka
 */
class SessionConfigurationClient {

    private final NttPolynomial piNtt;
    private final NttPolynomial pjNtt;
    private final ByteArrayWrapper ski;
    private final SessionConfigurationServer scs;

    SessionConfigurationClient(NttPolynomial piNtt, NttPolynomial pjNtt, ByteArrayWrapper ski, SessionConfigurationServer scs) {
        this.piNtt = piNtt;
        this.pjNtt = pjNtt;
        this.ski = ski;
        this.scs = scs;
    }

    NttPolynomial getClientsEphPubKey() {
        return piNtt;
    }

    NttPolynomial getServersEphPubKey() {
        return pjNtt;
    }

    ByteArrayWrapper getSharedSecret() {
        return ski;
    }

    SessionConfigurationServer getServersSessionConfiguration() { return scs; }
}
