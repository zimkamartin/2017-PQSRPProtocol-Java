package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

public class SessionConfigurationServer {

    private final NttPolynomial piNtt;
    private final NttPolynomial pjNtt;
    private final ByteArrayWrapper skj;

    SessionConfigurationServer(NttPolynomial piNtt, NttPolynomial pjNtt, ByteArrayWrapper skj) {
        this.piNtt = piNtt.defensiveCopy();
        this.pjNtt = pjNtt.defensiveCopy();
        this.skj = skj.defensiveCopy();
    }

    NttPolynomial getClientsEphPubKey() {
        return piNtt.defensiveCopy();
    }

    NttPolynomial getServersEphPubKey() {
        return pjNtt.defensiveCopy();
    }

    ByteArrayWrapper getSharedSecret() {
        return skj.defensiveCopy();
    }
}
