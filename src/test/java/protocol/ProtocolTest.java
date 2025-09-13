package protocol;

import org.junit.Test;
import protocol.client.ClientImple;
import protocol.client.ClientsKnowledge;
import protocol.client.LoginResponse;
import protocol.random.RandomCustomImple;
import protocol.server.Server;
import protocol.server.ServerImple;
import protocol.server.TestServerWrapper;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtocolTest {

    private static final int NUMBEROFROUNDS = 127;  // make sure that it is not larger than 127
    // (because of the way of changing client's identity and password so they are unique)
    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final ByteArrayWrapper I = new ByteArrayWrapper("identity123".getBytes());
    private static final ByteArrayWrapper PWD = new ByteArrayWrapper("password123".getBytes());
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    private ByteArrayWrapper generateNewI(int round) {
        byte[] identity = I.getData();
        identity[identity.length - 1] = (byte) round;
        return new ByteArrayWrapper(identity);
    }

    private ByteArrayWrapper generateNewPWD(int round) {
        byte[] password = PWD.getData();
        password[password.length - 1] = (byte) round;
        return new ByteArrayWrapper(password);
    }

    @Test
    public void bothPartiesSameKey() {

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            Server delegate = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);
            TestServerWrapper serverWrapper = new TestServerWrapper(delegate);

            ClientsKnowledge ck = new ClientsKnowledge(generateNewI(i), PWD);
            // Identity have to be different for every run since database is static.
            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), serverWrapper);

            client.enroll(ck);
            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            assertEquals(loginResponse.getSharedSecret(), serverWrapper.getCapturedSkj());
        }
    }
}