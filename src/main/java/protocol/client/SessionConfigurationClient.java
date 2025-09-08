package protocol.client;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;
import protocol.server.SessionConfigurationServer;

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
        return piNtt.defensiveCopy();
    }

    NttPolynomial getServersEphPubKey() {
        return pjNtt.defensiveCopy();
    }

    ByteArrayWrapper getSharedSecret() {
        return ski.defensiveCopy();
    }

    SessionConfigurationServer getServersSessionConfiguration() { return scs; }
}
