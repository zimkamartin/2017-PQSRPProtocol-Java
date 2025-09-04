package protocol;

import org.junit.jupiter.api.Test;
import protocol.client.ClientImple;
import protocol.client.ClientsSecrets;
import protocol.exceptions.NotEnrolledClientException;
import protocol.server.Server;
import protocol.server.ServerImple;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientImpleTest {

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);
    private static final int ETA = 3;
    private static final byte[] PUBLICSEEDFORA = "publicSeedForA123".getBytes();
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();
    private static final byte[] SALT = "salt123".getBytes();
    private static final List<BigInteger> VNTT = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3));
    private static final List<BigInteger> PINTT = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.valueOf(7));

    @Test
    void computeSharedSecretClientExists() {
        ClientsSecrets cs = new ClientsSecrets(I, PWD);
        Server server = new ServerImple(new SecureRandom(), N, Q, ETA);
        ClientImple client = new ClientImple(new SecureRandom(), server);
        client.enrollClient(cs);
        assertDoesNotThrow(() -> client.computeSharedSecret(cs));
    }

    @Test
    void computeSharedSecretClientDoesNotExist() {
        byte[] notEnrolledI = "notEnrolledI".getBytes();
        ClientsSecrets cs = new ClientsSecrets(I, PWD);
        Server server = new ServerImple(new SecureRandom(), N, Q, ETA);
        ClientImple client = new ClientImple(new SecureRandom(), server);
        client.enrollClient(cs);
        assertThrows(NotEnrolledClientException.class,
                () -> client.computeSharedSecret(new ClientsSecrets(notEnrolledI, PWD)));
    }
}