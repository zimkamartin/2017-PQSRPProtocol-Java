package protocol;

import protocol.client.ClientImple;
import protocol.client.ClientsKnowledge;
import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.exceptions.ServerNotAuthenticatedException;
import protocol.random.RandomCustomImple;
import protocol.server.Server;
import protocol.server.ServerImple;

import java.math.BigInteger;
import java.util.Arrays;

public class Main {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final ByteArrayWrapper I = new ByteArrayWrapper("identity123".getBytes());
    private static final ByteArrayWrapper PWD = new ByteArrayWrapper("password123".getBytes());
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    public static void main(String[] args) throws NotEnrolledClientException, ServerNotAuthenticatedException, ClientNotAuthenticatedException {
        ClientsKnowledge ck = new ClientsKnowledge(I.defensiveCopy(), PWD.defensiveCopy());
        Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);
        ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);
        client.enroll(ck);
        ByteArrayWrapper clientsKey = client.login(ck);
        System.out.println(Arrays.toString(clientsKey.getData()));
    }
}