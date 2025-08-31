package protocol;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();

    @Test
    void generatesDifferentKeysInTime() {
        int numOfIterations = 100;
        Set<ByteArrayWrapper> keys = new HashSet<>();
        for (int i = 0; i < numOfIterations; i++) {
            ClientsSecrets cs = new ClientsSecrets(I, PWD);
            Server server = new ServerImple(new SecureRandom(), N, Q, ETA);
            ClientImple client = new ClientImple(new SecureRandom(), server);
            client.enrollClient(cs);
            assertDoesNotThrow(() -> client.computeSharedSecret(cs));
            byte[] keyByteArray = assertDoesNotThrow(() -> client.verifyEntities());
            keys.add(new ByteArrayWrapper(keyByteArray));
        }
        assertEquals(numOfIterations, keys.size());
    }

    @Test
    void testDeterminism() {
        int numOfIterations = 100;
        Set<ByteArrayWrapper> keys = new HashSet<>();
        for (int i = 0; i < numOfIterations; i++) {
            ClientsSecrets cs = new ClientsSecrets(I, PWD);
            Server server = new ServerImple(new Random(123), N, Q, ETA);
            ClientImple client = new ClientImple(new Random(456), server);
            client.enrollClient(cs);
            assertDoesNotThrow(() -> client.computeSharedSecret(cs));
            byte[] keyByteArray = assertDoesNotThrow(() -> client.verifyEntities());
            keys.add(new ByteArrayWrapper(keyByteArray));
        }

        // Test that there is only 1 key. //
        assertEquals(1, keys.size());

        // Test that it is the correct key. //
        int[] keyIntArray = {114, 88, 85, -57, 42, 90, -20, 11, 15, -75, 103, 25, 59, 66, -62, 109, 101, -98, 19, -66, 30, -97, 127, -97, -112, -83, -102, -33, 93, -126, 96, -27};
        byte[] keyByteArray = new byte[keyIntArray.length];
        for (int i = 0; i < keyIntArray.length; i++) {
            keyByteArray[i] = (byte) keyIntArray[i];
        }
        ByteArrayWrapper keyByteArrayWrapped = new ByteArrayWrapper(keyByteArray);
        for (ByteArrayWrapper k: keys) {
            assertEquals(keyByteArrayWrapped, k);
        }
    }
}