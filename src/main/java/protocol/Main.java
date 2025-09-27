package protocol;

import protocol.client.ClientImple;
import protocol.client.ClientsKnowledge;
import protocol.client.LoginResponse;
import protocol.random.RandomCustomImple;
import protocol.server.Server;
import protocol.server.ServerImple;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * The {@code Main} class is the entry point of the application.
 * It configures protocol parameters and clients secrets. Also, it invokes {@link #demo()}
 * to demonstrate <a href="https://eprint.iacr.org/2017/1196.pdf">the protocol</a> implemented in this project.
 *
 * <p>Protocol parameters and clients secrets are the following:</p>
 * <ul>
 *   <li>{@code N}   – {@code int}, all polynomials are of degree N-1 (they are reduced modulo (X^N + 1))</li>
 *   <li>{@code Q}   – {@code BigInteger}, Q-1 is maximal value of polynomial coefficients (they are reduced modulo Q)</li>
 *   <li>{@code ETA} – {@code int}, bounds error polynomial values in range [-ETA, +ETA], sampled from a Centered
 *                                  Binomial Distribution</li>
 *   <li>{@code I}   – {@code ByteArrayWrapper}, represents client identity</li>
 *   <li>{@code PWD} – {@code ByteArrayWrapper}, represents client password</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class Main {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private static final ByteArrayWrapper I = new ByteArrayWrapper("identity123".getBytes());
    private static final ByteArrayWrapper PWD = new ByteArrayWrapper("password123".getBytes());
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    private static void demo() {
        Server server = new ServerImple(new RandomCustomImple(N, Q, ETA), N, Q, ETA);

        ClientsKnowledge ck = new ClientsKnowledge(I, PWD);
        ClientImple client = new ClientImple(new RandomCustomImple(N, Q, ETA), server);

        // Client decides to enroll //
        client.enroll(ck);
        // Client decides to log in //
        LoginResponse loginResponse = client.login(ck);
        String infoToTheConsole = loginResponse.getLoginOK() ? Arrays.toString(loginResponse.getSharedSecret().getData()) : "Login FAILED";
        System.out.println(infoToTheConsole);
    }

    public static void main(String[] args) {

        demo();

    }
}
