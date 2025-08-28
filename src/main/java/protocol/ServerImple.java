package protocol;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImple implements Server {

    private final PublicParams publicParams;
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;
    private final Ntt ntt;
    private final Map<ByteArrayWrapper, ClientsPublics> database = new HashMap<>();

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
        database.put(new ByteArrayWrapper(I), new ClientsPublics(publicSeedForA, salt, vNtt));
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
