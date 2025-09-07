package protocol.client;

import protocol.ByteArrayWrapper;

public class ClientsKnowledge {

    private final ByteArrayWrapper identity;
    private final ByteArrayWrapper password;

    public ClientsKnowledge(ByteArrayWrapper identity, ByteArrayWrapper password) {
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
