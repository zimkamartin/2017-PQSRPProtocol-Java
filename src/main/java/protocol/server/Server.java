package protocol.server;

import protocol.ProtocolConfiguration;
import protocol.SaltEphPublicSignal;
import protocol.exceptions.ClientNotAuthenticatedException;
import protocol.exceptions.NotEnrolledClientException;
import protocol.polynomial.Polynomial;

import java.math.BigInteger;
import java.util.List;

/**
 * Server from https://eprint.iacr.org/2017/1196.pdf
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
    void enrollClient(byte[] publicSeedForA, byte[] I, byte[] salt, Polynomial vNtt);

    /**
     * @param I - client's identity
     * @param piNtt - polynomial representing client's ephemeral public key in NTT form
     * @return client's salt, polynomial representing server's ephemeral public key, polynomial where coefficients are result of applying Signal function
     */
    SaltEphPublicSignal computeSharedSecret(byte[] I, Polynomial piNtt) throws NotEnrolledClientException;

    /**
     * @param m1 - hash of concatenated client's ephemeral key with server's ephemeral key and with client's shared secret key
     * @return hash of concatenated client's ephemeral key with m1 with server's shared secret key
     */
    byte[] verifyEntities(byte[] m1) throws ClientNotAuthenticatedException;
}
