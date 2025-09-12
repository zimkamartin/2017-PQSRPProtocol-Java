package protocol.client;

import protocol.ByteArrayWrapper;

public class ClientsKnowledge {

    private final ByteArrayWrapper identity;
    private final ByteArrayWrapper password;

    public ClientsKnowledge(ByteArrayWrapper identity, ByteArrayWrapper password) {
        this.identity = identity;
        this.password = password;
    }

    ByteArrayWrapper getIdentity() {
        return identity;
    }

    ByteArrayWrapper getPassword() {
        return password;
    }
}
