package protocol;

import protocol.polynomial.NttPolynomial;

public class SessionConfiguration {

    private NttPolynomial piNtt;
    private NttPolynomial pjNtt;
    private byte[] sk;

    public SessionConfiguration() {}

    public void setClientsEphPubKey(NttPolynomial piNtt) {
        this.piNtt = piNtt.defensiveCopy();
    }

    public void setServersEphPubKey(NttPolynomial pjNtt) {
        this.pjNtt = pjNtt.defensiveCopy();
    }

    public void setSharedSecret(byte[] sk) {
        this.sk = sk.clone();
    }

    public NttPolynomial getClientsEphPubKey() {
        return piNtt.defensiveCopy();
    }

    public NttPolynomial getServersEphPubKey() {
        return pjNtt.defensiveCopy();
    }

    public byte[] getSharedSecret() {
        return sk.clone();
    }
}
