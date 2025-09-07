package protocol;

import protocol.polynomial.NttPolynomial;

public class SessionConfiguration {

    private NttPolynomial piNtt;
    private NttPolynomial pjNtt;
    private ByteArrayWrapper sk;

    public SessionConfiguration() {}

    public void setClientsEphPubKey(NttPolynomial piNtt) {
        this.piNtt = piNtt.defensiveCopy();
    }

    public void setServersEphPubKey(NttPolynomial pjNtt) {
        this.pjNtt = pjNtt.defensiveCopy();
    }

    public void setSharedSecret(ByteArrayWrapper sk) {
        this.sk = sk.defensiveCopy();
    }

    public NttPolynomial getClientsEphPubKey() {
        return piNtt.defensiveCopy();
    }

    public NttPolynomial getServersEphPubKey() {
        return pjNtt.defensiveCopy();
    }

    public ByteArrayWrapper getSharedSecret() {
        return sk.defensiveCopy();
    }
}
