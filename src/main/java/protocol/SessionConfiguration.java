package protocol;

import protocol.polynomial.Polynomial;

public class SessionConfiguration {

    private Polynomial piNtt;
    private Polynomial pjNtt;
    private byte[] sk;

    public SessionConfiguration() {}

    public void setClientsEphPubKey(Polynomial piNtt) {
        this.piNtt = piNtt.defensiveCopy();
    }

    public void setServersEphPubKey(Polynomial pjNtt) {
        this.pjNtt = pjNtt.defensiveCopy();
    }

    public void setSharedSecret(byte[] sk) {
        this.sk = sk.clone();
    }

    public Polynomial getClientsEphPubKey() {
        return piNtt.defensiveCopy();
    }

    public Polynomial getServersEphPubKey() {
        return pjNtt.defensiveCopy();
    }

    public byte[] getSharedSecret() {
        return sk.clone();
    }
}
