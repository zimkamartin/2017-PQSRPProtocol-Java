package protocol;

import org.junit.jupiter.api.Test;
import protocol.exceptions.NotEnrolledClientException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerImpleTest {

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);
    private static final int ETA = 3;
    private static final byte[] PUBLICSEEDFORA = "publicSeedForA123".getBytes();
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] SALT = "salt123".getBytes();
    private static final List<BigInteger> VNTT = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3));
    private static final List<BigInteger> PINTT = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.valueOf(7));

    @Test
    void computeSharedSecretClientExists() {
        Server server = new ServerImple(N, Q, ETA);
        server.enrollClient(PUBLICSEEDFORA,  I, SALT, VNTT);
        assertDoesNotThrow(() -> server.computeSharedSecret(I, PINTT));
    }

    @Test
    void computeSharedSecretClientDoesNotExist() {
        byte[] notEnrolledI = "notEnrolledI".getBytes();
        Server server = new ServerImple(N, Q, ETA);
        server.enrollClient(PUBLICSEEDFORA, I, SALT, VNTT);
        assertThrows(NotEnrolledClientException.class,
                () -> server.computeSharedSecret(notEnrolledI, PINTT));
    }
}