package protocol;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Utils {

    private Utils(){}

    public static void getEtaNoise(PublicParams pp, Mlkem mlkem, Engine engine, List<BigInteger> r, byte[] seed) {
        byte[] buf = new byte[pp.getN() * pp.getEta() / 4];
        engine.prf(buf, seed);
        mlkem.generateCbdPolynomial(r, buf, pp.getEta());
    }

    public static List<BigInteger> generateRandomErrorPolyNtt(PublicParams pp, Mlkem mlkem, Engine engine, Ntt ntt) {
        List<BigInteger> e = new ArrayList<>(pp.getN());
        byte[] eRandomSeed = new byte[34];
        engine.getRandomBytes(eRandomSeed);
        getEtaNoise(pp, mlkem, engine, e, eRandomSeed);
        return ntt.convertFromNtt(e);
    }
}
