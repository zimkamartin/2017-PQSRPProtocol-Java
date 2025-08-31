package protocol;

import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.exceptions.ServerNotAuthenticatedException;

import java.math.BigInteger;
import java.util.Arrays;

public class Main {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final byte[] I = "identity123".getBytes();
    private static final byte[] PWD = "password123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    public static void main(String[] args) throws NotEnrolledClientException, ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        ClientsSecrets cs = new ClientsSecrets(I, PWD);
        Server server = new ServerImple(N, Q, ETA);
        ClientImple client = new ClientImple(server);
        client.enrollClient(cs);
        client.computeSharedSecret(cs);
        byte[] clientsKey = client.verifyEntities();
        System.out.println(Arrays.toString(clientsKey));
    }
}