package protocol;

import org.junit.Test;
import protocol.client.ClientImple;
import protocol.client.ClientsKnowledge;
import protocol.client.LoginResponse;
import protocol.random.TestPreSeededRandom;
import protocol.random.RandomCustomImple;
import protocol.server.Server;
import protocol.server.ServerImple;
import protocol.server.TestServerWrapper;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The {@code ProtocolTest} class tests the protocol as a complete system.
 *
 * <p>The following aspects are verified:</p>
 * <ul>
 *     <li>the client and server derive the same shared secret key</li>
 *     <li>different clients derive different shared secret keys</li>
 *     <li>multiple logins after the same enrollment produce distinct shared secret keys</li>
 *     <li>the protocol’s deterministic components results in the same and correct shared secret key</li>
 *     <li>login fails when using an incorrect verifier</li>
 *     <li>multiple parties can enroll and log in concurrently without interference</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class ProtocolTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;
    // Size of randomly generated identity.
    private static final int RANDOMISIZE = 11;
    // Size of randomly generated password.
    private static final int RANDOMPWDSIZE = 11;

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final ByteArrayWrapper I = new ByteArrayWrapper("identity123".getBytes());
    private static final ByteArrayWrapper PWD = new ByteArrayWrapper("password123".getBytes());
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    // Generate random bytes to generate random identity or random password.
    Random random = new Random();

    private ByteArrayWrapper generateRandomI() {
        byte[] identity = new byte[RANDOMISIZE];
        random.nextBytes(identity);
        return new ByteArrayWrapper(identity);
    }

    private ByteArrayWrapper generateRandomPWD() {
        byte[] password = new byte[RANDOMPWDSIZE];
        random.nextBytes(password);
        return new ByteArrayWrapper(password);
    }

    /**
     * Generates {@code pseudo-random} byte[] object wrapped in class {@code ByteArrayWrapper}.
     *
     * @param seed seed for random generator
     * @return ByteArrayWrapper object with pseudo-random byte[] data
     */
    private ByteArrayWrapper generateSeededBAW(long seed) {

        Random random = new Random(seed);

        byte[] byteArray = new byte[123];  // randomly set value
        random.nextBytes(byteArray);

        return new ByteArrayWrapper(byteArray);
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
     * Tests determinism of the protocol and correctness of the computed key.
     * <p>Tests that {@code NUMBEROFROUNDS} client-server interactions without the randomness will end up
     * with the same correct key everytime.</p>
     */
    @Test
    public void determinismOneCorrectKey() {

        Set<ByteArrayWrapper> keys = new HashSet<>();
        ByteArrayWrapper correctKey = new ByteArrayWrapper(new byte[] {31, 41, -100, -114, 89, 36, -83, -10, 50, 119,
                -102, -11, -78, -21, -75, -110, -95, -64, 16, 106, 16, -70, 89, -111, 118, -102, 89, -42, 104, 95, -88, 2});

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            Server server = new ServerImple(new TestPreSeededRandom(N, Q, ETA, 123), N, Q, ETA);

            ClientsKnowledge ck = new ClientsKnowledge(I, PWD);
            ClientImple client = new ClientImple(new TestPreSeededRandom(N, Q, ETA, 456), server);

            client.enroll(ck);
            LoginResponse loginResponse = client.login(ck);

            assertTrue(loginResponse.getLoginOK());
            keys.add(loginResponse.getSharedSecret());
        }

        assertEquals(1, keys.size());
        assertTrue(keys.contains(correctKey));
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that login with incorrect verifier fails.
     */
    @Test
    public void incorrectVFails() {

        for (int i = 1; i < NUMBEROFROUNDS; i++) {

            ByteArrayWrapper identity = generateSeededBAW(i);
            ByteArrayWrapper passwordEnroll = generateSeededBAW(2L * i);
            ByteArrayWrapper passwordLogin = generateSeededBAW(3L * i);

            Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

            ClientsKnowledge ckEnroll = new ClientsKnowledge(identity, passwordEnroll);
            ClientsKnowledge ckLogin = new ClientsKnowledge(identity, passwordLogin);

            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

            client.enroll(ckEnroll);
            LoginResponse loginResponse = client.login(ckLogin);

            assertFalse(loginResponse.getLoginOK());
        }
    }

    /**
     * Tests that when {@code NUMBEROFROUNDS} parties enroll to the same server and then try to log in,
     * every log in will end with a success.
     */
    @Test
    public void enrolledMultiplePartiesLoginsSucceeded() {

        Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

        ClientImple[] clients = new ClientImple[NUMBEROFROUNDS];

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            ByteArrayWrapper identity = generateSeededBAW(i);
            ByteArrayWrapper password = generateSeededBAW(2L * i);

            ClientsKnowledge ck = new ClientsKnowledge(identity, password);
            ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

            client.enroll(ck);
            clients[i] = client;
        }

        for (int i = NUMBEROFROUNDS - 1; i >= 0; i--) {

            ByteArrayWrapper identity = generateSeededBAW(i);
            ByteArrayWrapper password = generateSeededBAW(2L * i);

            ClientsKnowledge ck = new ClientsKnowledge(identity, password);
            ClientImple client = clients[i];

            LoginResponse loginResponse = client.login(ck);
            assertTrue(loginResponse.getLoginOK());
        }
    }
}