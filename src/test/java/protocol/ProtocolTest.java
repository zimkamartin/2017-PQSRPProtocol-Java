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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtocolTest {

    private static final int NUMBEROFROUNDS = 111;
    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final ByteArrayWrapper I = new ByteArrayWrapper("identity123".getBytes());
    private static final ByteArrayWrapper PWD = new ByteArrayWrapper("password123".getBytes());
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    Random random = new Random();

    private ByteArrayWrapper generateRandomI() {
        byte[] identity = I.getData();
        identity[identity.length - 1] = (byte) random.nextInt(256);
        return new ByteArrayWrapper(identity);
    }

    private ByteArrayWrapper generateRandomPWD() {
        byte[] password = PWD.getData();
        password[password.length - 1] = (byte) random.nextInt(256);
        return new ByteArrayWrapper(password);
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that the same client with the (same) server will at the end have the same key.
     */
    @Test
    public void samePartiesBothSameKey() {

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            Server delegate = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);
            TestServerWrapper serverWrapper = new TestServerWrapper(delegate);

            ClientsKnowledge ck = new ClientsKnowledge(I, PWD);
            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), serverWrapper);

            client.enroll(ck);
            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            assertEquals(loginResponse.getSharedSecret(), serverWrapper.getCapturedSkj());
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that different client with the (same) server will at the end have the same key.
     */
    @Test
    public void differentPartiesBothSameKey() {

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            Server delegate = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);
            TestServerWrapper serverWrapper = new TestServerWrapper(delegate);

            ClientsKnowledge ck = new ClientsKnowledge(generateRandomI(), generateRandomPWD());
            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), serverWrapper);

            client.enroll(ck);
            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            assertEquals(loginResponse.getSharedSecret(), serverWrapper.getCapturedSkj());
        }
    }

    /**
     * Tests that {@code NUMBEROFROUNDS} different clients will at the end have different key.
     */
    @Test
    public void differentClientsDifferentKeys() {

        Set<ByteArrayWrapper> keys = new HashSet<>();

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

            ClientsKnowledge ck = new ClientsKnowledge(generateRandomI(), generateRandomPWD());
            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

            client.enroll(ck);
            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            keys.add(loginResponse.getSharedSecret());
        }

        assertEquals(NUMBEROFROUNDS, keys.size());
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that the same client will end up with different key everytime he logs to the (same) server.
     */
    @Test
    public void samePartiesDifferentLoginsDifferentKeys() {

        Set<ByteArrayWrapper> keys = new HashSet<>();

        Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

        ClientsKnowledge ck = new ClientsKnowledge(I, PWD);
        ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

        client.enroll(ck);

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            keys.add(loginResponse.getSharedSecret());
        }

        assertEquals(NUMBEROFROUNDS, keys.size());
    }

    /**
     * Tests determinism of the protocol.
     * <p>Tests {@code NUMBEROFROUNDS}-times that the same client-server with not changing ephemeral keys
     * will end up with the same key everytime.</p>
     */
    @Test
    public void samePartiesSameEphKeysSameKeys() {

        // TODO
    }

    // TODO: Enrolled with one verifier, login with other => fail
    // TODO: UNIT tests
}