package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

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
