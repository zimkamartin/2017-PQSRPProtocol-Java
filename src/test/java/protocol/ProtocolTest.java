package protocol;

import java.math.BigInteger;

class ProtocolTest {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();

//    @Test
//    void generatesDifferentKeysInTime() {
//        int numOfIterations = 100;
//        Set<ByteArrayWrapper> keys = new HashSet<>();
//        for (int i = 0; i < numOfIterations; i++) {
//            ClientsSecrets cs = new ClientsSecrets(I, PWD);
//            Server server = new ServerImple(new SecureRandom(), N, Q, ETA);
//            ClientImple client = new ClientImple(new SecureRandom(), server);
//            client.enrollClient(cs);
//            assertDoesNotThrow(() -> client.computeSharedSecret(cs));
//            byte[] keyByteArray = assertDoesNotThrow(() -> client.verifyEntities());
//            keys.add(new ByteArrayWrapper(keyByteArray));
//        }
//        assertEquals(numOfIterations, keys.size());
//    }
//
//    @Test
//    void testDeterminism() {
//        int numOfIterations = 100;
//        Set<ByteArrayWrapper> keys = new HashSet<>();
//        for (int i = 0; i < numOfIterations; i++) {
//            ClientsSecrets cs = new ClientsSecrets(I, PWD);
//            Server server = new ServerImple(new Random(123), N, Q, ETA);
//            ClientImple client = new ClientImple(new Random(456), server);
//            client.enrollClient(cs);
//            assertDoesNotThrow(() -> client.computeSharedSecret(cs));
//            byte[] keyByteArray = assertDoesNotThrow(() -> client.verifyEntities());
//            keys.add(new ByteArrayWrapper(keyByteArray));
//        }
//
//        // Test that there is only 1 key. //
//        assertEquals(1, keys.size());
//
//        // Test that it is the correct key. //
//        int[] keyIntArray = {31, 41, -100, -114, 89, 36, -83, -10, 50, 119, -102, -11, -78, -21, -75, -110, -95, -64, 16, 106, 16, -70, 89, -111, 118, -102, 89, -42, 104, 95, -88, 2};
//        byte[] keyByteArray = new byte[keyIntArray.length];
//        for (int i = 0; i < keyIntArray.length; i++) {
//            keyByteArray[i] = (byte) keyIntArray[i];
//        }
//        ByteArrayWrapper keyByteArrayWrapped = new ByteArrayWrapper(keyByteArray);
//        for (ByteArrayWrapper k: keys) {
//            assertEquals(keyByteArrayWrapped, k);
//        }
//    }
}