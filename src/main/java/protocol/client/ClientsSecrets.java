package protocol.client;

public class ClientsSecrets {

    private final byte[] identity;
    private final byte[] password;

    public ClientsSecrets(byte[] identity, byte[] password) {
        this.identity = identity;
        this.password = password;
    }

    byte[] getIdentity() {
        return identity;
    }

    byte[] getPassword() {
        return password;
    }
}
