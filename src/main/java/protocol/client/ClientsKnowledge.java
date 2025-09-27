package protocol.client;

import protocol.ByteArrayWrapper;

/**
 * The {@code ClientsKnowledge} class represents a client’s knowledge within the protocol.
 *
 * <p>The client’s knowledge consists of:</p>
 * <ul>
 *   <li>{@code identity} – {@code ByteArrayWrapper}, the client’s identity</li>
 *   <li>{@code password} – {@code ByteArrayWrapper}, the client’s password</li>
 * </ul>
 *
 * @author Martin Zimka
 */
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
