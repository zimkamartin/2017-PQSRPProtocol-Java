package protocol.client;

import protocol.ByteArrayWrapper;

/**
 * The {@code LoginResponse} class represents the response a client receives when attempting to log in.
 *
 * <p>The login response consists of:</p>
 * <ul>
 *   <li>{@code loginOK}      – {@code boolean}, indicates whether the login was successful</li>
 *   <li>{@code sharedSecret} – {@code ByteArrayWrapper}, the shared secret computed on the client’s side
 *       (meaningful only if {@code loginOK} is {@code true})</li>
 * </ul>
 *
 * @author Martin Zimka
 */
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
