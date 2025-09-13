package protocol.client;

import protocol.ByteArrayWrapper;

public class LoginResponse {

    private final boolean loginOK;
    private final ByteArrayWrapper sharedSecret;

    public LoginResponse(boolean loginOk, ByteArrayWrapper sharedSecret) {
        this.loginOK = loginOk;
        this.sharedSecret = sharedSecret;
    }

    public boolean getLoginOK() {
        return loginOK;
    }

    public ByteArrayWrapper getSharedSecret() {
        return sharedSecret;
    }
}
