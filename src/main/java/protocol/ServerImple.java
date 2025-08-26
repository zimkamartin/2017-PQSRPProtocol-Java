package protocol;

import java.math.BigInteger;
import java.util.List;

public class ServerImple implements Server {

    private final PublicParams publicParams;
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;

    public ServerImple(int n, BigInteger q, int eta) {
        this.publicParams = new PublicParams(n, q, eta);
        this.mlkem = new MlkemImple(publicParams.getN(), publicParams.getQ());
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
