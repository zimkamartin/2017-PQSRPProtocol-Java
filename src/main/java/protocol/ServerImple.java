package protocol;

import java.math.BigInteger;
import java.util.List;

public class ServerImple implements Server {

    private final PublicParams publicParams;

    public ServerImple(int n, BigInteger q) {
        this.publicParams = new PublicParams(n, q);
    }

    @Override
    public PublicParams getPublicParams() {
        return publicParams;
    }

    @Override
    public byte[] generatePublicSeedForA() {
        return new byte[0];
    }

    @Override
    public void enrollClient(String I, byte[] salt, List<BigInteger> vNtt) {

    }

    @Override
    public SaltEphPublicSignal computeSharedSecret(String I, BigInteger[] pi) {
        return null;
    }

    @Override
    public byte[] verifyEntities(byte[] m1) {
        return new byte[0];
    }
}
