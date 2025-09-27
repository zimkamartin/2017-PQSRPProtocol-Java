package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.ProtocolConfiguration;
import protocol.ServersResponseScs;
import protocol.polynomial.NttPolynomial;

/**
 * The {@code Server} interface defines the server-side operations of the protocol
 * described in <a href="https://eprint.iacr.org/2017/1196.pdf">ePrint 2017/1196</a>.
 *
 * <p>The interface provides the following methods:</p>
 * <ul>
 *   <li>{@link #getProtocolConfiguration()} – returns the protocol configuration: {@code n}, {@code q}, {@code eta}</li>
 *   <li>{@link #enrollClient(ByteArrayWrapper, ByteArrayWrapper, ByteArrayWrapper, NttPolynomial)} –
 *       stores a new client record in the server’s database, consisting of
 *       the client’s identity, salt, public seed (for generating the public
 *       polynomial {@code a}), and verifier in NTT form</li>
 *   <li>{@link #computeSharedSecret(ByteArrayWrapper, NttPolynomial)} –
 *       given a client’s identity and client's ephemeral public key (in NTT form),
 *       computes and returns a {@link ServersResponseScs} object containing:
 *       <ul>
 *         <li>the client’s salt,</li>
 *         <li>the server’s ephemeral public key (in NTT form), and</li>
 *         <li>a polynomial encoding the output of the Signal function</li>
 *       </ul>
 *   </li>
 *   <li>{@link #verifyEntities(SessionConfigurationServer, ByteArrayWrapper)} –
 *       performs mutual authentication between the client and server by verifying
 *       the client’s message {@code m1}, and returning the corresponding
 *       server response. This ensures parties are authenticated and both have derived the same
 *       shared secret.</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public interface Server {

    /**
     * @return n, q, eta
     */
    ProtocolConfiguration getProtocolConfiguration();

    /**
     * @param publicSeedForA - public seed that will be used to generate public polynomial a
     * @param I - client's identity
     * @param salt - client's salt
     * @param vNtt- polynomial representing client's verifier in NTT form
     */
    void enrollClient(ByteArrayWrapper publicSeedForA, ByteArrayWrapper I, ByteArrayWrapper salt, NttPolynomial vNtt);

    /**
     * @param I - client's identity
     * @param piNtt - polynomial representing client's ephemeral public key in NTT form
     * @return client's salt, polynomial representing server's ephemeral public key, polynomial where coefficients are result of applying Signal function
     */
    ServersResponseScs computeSharedSecret(ByteArrayWrapper I, NttPolynomial piNtt);

    /**
     * @param m1 - hash of concatenated client's ephemeral key with server's ephemeral key and with client's shared secret key
     * @return hash of concatenated client's ephemeral key with m1 with server's shared secret key
     */
    ByteArrayWrapper verifyEntities(SessionConfigurationServer scs, ByteArrayWrapper m1);
}
