package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

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
