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
 *         <li>the client’s salt</li>
 *         <li>the server’s ephemeral public key (in NTT form)</li>
 *         <li>a polynomial encoding the output of the Signal function applied to key exchange material</li>
 *         <li>server's session configuration (allowing the server to continue with other clients)</li>
 *       </ul>
 *   </li>
 *   <li>{@link #verifyKeysEntities(SessionConfigurationServer, ByteArrayWrapper)} –
 *       verifies that both parties constructed the same key and authenticate client.
 *       Firstly verify client’s message {@code m1}, and then return the corresponding server response.
 *   </li>
 * </ul>
 *
 * @author Martin Zimka
 */
public interface Server {

    /**
     * Returns the protocol configuration.
     *
     * @return n, q, eta encapsulated in {@link ProtocolConfiguration} object
     */
    ProtocolConfiguration getProtocolConfiguration();

    /**
     * Enrolls client.
     *
     * @param publicSeedForA - public seed that will be used to generate public polynomial a
     * @param I - client's identity
     * @param salt - client's salt
     * @param vNtt- polynomial representing client's verifier in NTT form
     */
    void enrollClient(ByteArrayWrapper publicSeedForA, ByteArrayWrapper I, ByteArrayWrapper salt, NttPolynomial vNtt);

    /**
     * Computes shared secret on server's side for session client-server determined by client's information in attributes.
     *
     * @param I - client's identity
     * @param piNtt - polynomial representing client's ephemeral public key in NTT form
     * @return client's salt, polynomial representing server's ephemeral public key,
     * polynomial where coefficients are result of applying Signal function to key exchange material
     * and server's session configuration. All encapsulated in {@link ServersResponseScs} object.
     */
    ServersResponseScs computeSharedSecret(ByteArrayWrapper I, NttPolynomial piNtt);

    /**
     * Verifies that both parties have derived the same key and authenticate client.
     *
     * @param scs - server's configuration of a session with the client
     * @param m1 - hash of concatenated client's ephemeral key with server's ephemeral key and with client's shared secret key
     * @return hash of concatenated client's ephemeral key with m1 with server's shared secret key
     * or null if obtained attribute failed verification
     */
    ByteArrayWrapper verifyKeysEntities(SessionConfigurationServer scs, ByteArrayWrapper m1);
}
