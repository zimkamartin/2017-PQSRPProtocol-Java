package protocol.server;

import protocol.polynomial.NttPolynomial;
import protocol.polynomial.Polynomial;

public class ClientRecord {

    private final byte[] publicSeedForA;
    private final byte[] salt;
    private final NttPolynomial verifierNtt;

    public ClientRecord(byte[] publicSeedForA, byte[] salt, NttPolynomial verifierNtt) {
        this.publicSeedForA = publicSeedForA;
        this.salt = salt;
        this.verifierNtt = verifierNtt.defensiveCopy();
    }

    public byte[] getPublicSeedForA() {
        return this.publicSeedForA;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public NttPolynomial getVerifierNtt() {
        return this.verifierNtt.defensiveCopy();
    }
}
