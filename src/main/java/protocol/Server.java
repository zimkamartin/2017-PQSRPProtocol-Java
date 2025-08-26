package protocol;

import java.math.BigInteger;
import java.util.List;

public interface Server {
    PublicParams getPublicParams();
    byte[] generatePublicSeedForA();
    void enrollClient(String I, byte[] salt, List<BigInteger> vNtt);
    SaltEphPublicSignal computeSharedSecret(String I, BigInteger[] pi);
    byte[] verifyEntities(byte[] m1);
}
