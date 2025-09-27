package protocol.server;

import protocol.ByteArrayWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ServersDatabase} class represents the server-side client database.
 * <p>
 * It stores client records, keyed by client identity. Each record contains
 * the public seed for {@code a}, salt, and verifier in NTT form.
 * </p>
 *
 * @author Martin Zimka
 */
final class ServersDatabase {

    private static final Map<ByteArrayWrapper, ClientRecord> database = new HashMap<>();

    private ServersDatabase(){}

    static void saveClient(ByteArrayWrapper identity, ClientRecord clientRecord) {
        if (database.containsKey(identity)){
            throw new IllegalArgumentException("Client already exists");
        }
        database.put(identity, clientRecord);
    }

    static ClientRecord getClient(ByteArrayWrapper identity) {
        if (!database.containsKey(identity)){
            throw new IllegalArgumentException("Client does not exist");
        }
        return database.get(identity);
    }

    static boolean contains(ByteArrayWrapper identity) {
        return database.containsKey(identity);
    }
}
