package protocol.server;

import java.math.BigInteger;
import java.util.List;

public class ClientRecord {

    private final byte[] publicSeedForA;
    private final byte[] salt;
    private final List<BigInteger> verifierNtt;

    public ClientRecord(byte[] publicSeedForA, byte[] salt, List<BigInteger> verifierNtt) {
        this.publicSeedForA = publicSeedForA;
        this.salt = salt;
        this.verifierNtt = verifierNtt;
    }

    public byte[] getPublicSeedForA() {
        return this.publicSeedForA;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public List<BigInteger> getVerifierNtt() {
        return this.verifierNtt;
    }
}
