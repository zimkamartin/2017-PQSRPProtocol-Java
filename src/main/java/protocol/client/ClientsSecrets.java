package protocol.client;

import protocol.ByteArrayWrapper;

public class ClientsSecrets {

    private final ByteArrayWrapper identity;
    private final ByteArrayWrapper password;

    public ClientsSecrets(ByteArrayWrapper identity, ByteArrayWrapper password) {
        this.identity = identity.defensiveCopy();
        this.password = password.defensiveCopy();
    }

    ByteArrayWrapper getIdentity() {
        return identity.defensiveCopy();
    }

    ByteArrayWrapper getPassword() {
        return password.defensiveCopy();
    }
}
