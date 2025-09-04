package protocol;

import java.math.BigInteger;
import java.util.List;

public class SessionConfiguration {

    private List<BigInteger> piNtt;
    private List<BigInteger> pjNtt;
    private byte[] sk;

    public SessionConfiguration() {}

    public void setClientsEphPubKey(List<BigInteger> piNtt) {
        this.piNtt = List.copyOf(piNtt);
    }

    public void setServersEphPubKey(List<BigInteger> pjNtt) {
        this.pjNtt = List.copyOf(pjNtt);
    }

    public void setSharedSecret(byte[] sk) {
        this.sk = sk.clone();
    }

    public List<BigInteger> getClientsEphPubKey() {
        return List.copyOf(piNtt);
    }

    public List<BigInteger> getServersEphPubKey() {
        return List.copyOf(pjNtt);
    }

    public byte[] getSharedSecret() {
        return sk.clone();
    }
}
