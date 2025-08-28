package protocol;

import java.math.BigInteger;
import java.util.List;

public class ServerImple implements Server {

    private final PublicParams publicParams;
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;
    private final Ntt ntt;

    // THIS IS NOT HOW TO DO IT ! THIS IS JUST FOR PROOF-OF-CONCEPT ! THIS IS NOT HOW TO DO IT !
    // DATABASE //
    private byte[] publicSeedForA = null;
    private byte[] I = null;
    private byte[] salt = null;
    private List<BigInteger> vNtt = null;
    // THIS IS NOT HOW TO DO IT ! THIS IS JUST FOR PROOF-OF-CONCEPT ! THIS IS NOT HOW TO DO IT !

    public ServerImple(int n, BigInteger q, int eta) {
        this.publicParams = new PublicParams(n, q, eta);
        this.mlkem = new MlkemImple(publicParams.getN(), publicParams.getQ());
        this.ntt = new NttImple(publicParams.getN(), publicParams.getQ());
    }

    @Override
    public PublicParams getPublicParams() {
        return publicParams;
    }

    @Override
    public void enrollClient(byte[] publicSeedForA, byte[] I, byte[] salt, List<BigInteger> vNtt) {
        this.publicSeedForA = publicSeedForA;
        this.I = I;
        this.salt = salt;
        this.vNtt = vNtt;
    }

    @Override
    public SaltEphPublicSignal computeSharedSecret(byte[] I, List<BigInteger> piNtt) {
        return null;
    }

    @Override
    public byte[] verifyEntities(byte[] m1) {
        return new byte[0];
    }
}
