package protocol;

import java.math.BigInteger;
import java.util.List;

public class ClientsPublics {

    private final byte[] publicSeedForA;
    private final byte[] salt;
    private final List<BigInteger> verifier;

    public ClientsPublics(byte[] publicSeedForA, byte[] salt, List<BigInteger> verifier) {
        this.publicSeedForA = publicSeedForA;
        this.salt = salt;
        this.verifier = verifier;
    }

    public byte[] getPublicSeedForA() {
        return this.publicSeedForA;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public List<BigInteger> getVerifier() {
        return this.verifier;
    }
}
