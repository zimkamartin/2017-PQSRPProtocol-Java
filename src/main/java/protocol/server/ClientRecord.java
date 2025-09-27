package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

/**
 * The {@code ClientRecord} class represents a record in the server’s client database, indexed by clients IDs.
 *
 * <p>Each record consists of the following fields:</p>
 * <ul>
 *   <li>{@code publicSeedForA} – {@code ByteArrayWrapper}, the public seed used to generate the public polynomial {@code a}</li>
 *   <li>{@code salt}           – {@code ByteArrayWrapper}, the client’s salt</li>
 *   <li>{@code verifierNtt}    – {@code NttPolynomial}, the client’s verifier in NTT representation</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class ClientRecord {

    private final ByteArrayWrapper publicSeedForA;
    private final ByteArrayWrapper salt;
    private final NttPolynomial verifierNtt;

    public ClientRecord(ByteArrayWrapper publicSeedForA, ByteArrayWrapper salt, NttPolynomial verifierNtt) {
        this.publicSeedForA = publicSeedForA;
        this.salt = salt;
        this.verifierNtt = verifierNtt;
    }

    public ByteArrayWrapper getPublicSeedForA() {
        return publicSeedForA;
    }

    public ByteArrayWrapper getSalt() {
        return salt;
    }

    public NttPolynomial getVerifierNtt() {
        return verifierNtt;
    }
}
