package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.polynomial.NttPolynomial;

public class ClientRecord {

    private final ByteArrayWrapper publicSeedForA;
    private final ByteArrayWrapper salt;
    private final NttPolynomial verifierNtt;

    public ClientRecord(ByteArrayWrapper publicSeedForA, ByteArrayWrapper salt, NttPolynomial verifierNtt) {
        this.publicSeedForA = publicSeedForA.defensiveCopy();
        this.salt = salt.defensiveCopy();
        this.verifierNtt = verifierNtt.defensiveCopy();
    }

    public ByteArrayWrapper getPublicSeedForA() {
        return this.publicSeedForA.defensiveCopy();
    }

    public ByteArrayWrapper getSalt() {
        return this.salt.defensiveCopy();
    }

    public NttPolynomial getVerifierNtt() {
        return this.verifierNtt.defensiveCopy();
    }
}
