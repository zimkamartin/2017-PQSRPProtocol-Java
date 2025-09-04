package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.ClientsPublics;

import java.util.HashMap;
import java.util.Map;

/**
 * As name suggests, represents database located on the server's side.N and Q will be used to represent a polynomial modulo (X^N + 1) with all coefficients modulo Q.
 * <p>
 * It is database of clients.
 * The key is client's identity
 * and the value is seed for the public polynomial a, salt and verifier.
 * </p>
 */
final class ServersDatabase {

    private static final Map<ByteArrayWrapper, ClientsPublics> database = new HashMap<>();

    private ServersDatabase(){}

    static void saveClient(ByteArrayWrapper identity, ClientsPublics clientsPublics) {
        if (database.containsKey(identity)){
            throw new IllegalArgumentException("Client already exists");
        }
        database.put(identity, clientsPublics);
    }

    static ClientsPublics getClient(ByteArrayWrapper identity) {
        if (!database.containsKey(identity)){
            throw new IllegalArgumentException("Client does not exist");
        }
        return database.get(identity);
    }

    static boolean contains(ByteArrayWrapper identity) {
        return database.containsKey(identity);
    }
}
