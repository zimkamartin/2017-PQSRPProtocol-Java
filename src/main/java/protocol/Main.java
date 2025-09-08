package protocol;

import protocol.client.ClientImple;
import protocol.client.ClientsKnowledge;
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

    // mozno cisto bez mainu a v testoch sa to vsetko ukaze
    public static void main(String[] args) {

        Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

        ClientsKnowledge ck = new ClientsKnowledge(I.defensiveCopy(), PWD.defensiveCopy());
        ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

        // Client decides to enroll //
        client.enroll(ck);
        // Client decides to log in //
        ByteArrayWrapper clientsKey = client.login(ck);
        String infoToTheConsole = (clientsKey == null) ? "Login FAILED." : Arrays.toString(clientsKey.getData());
        System.out.println(infoToTheConsole);
    }
}