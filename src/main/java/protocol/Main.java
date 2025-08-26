package protocol;

import java.math.BigInteger;

public class Main {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    public static void main(String[] args) {
        Server server = new ServerImple(N, Q, ETA);
        ClientImple client = new ClientImple(server);
        client.enrollClient();
        client.computeSharedSecret();
        client.verifyEntities();

    }
}