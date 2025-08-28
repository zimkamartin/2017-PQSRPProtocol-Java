package protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Utils {

    private Utils(){}

    public static byte[] convertBigIntegerListToByteArray(List<BigInteger> inp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (BigInteger coeff : inp) {
                out.write(coeff.toByteArray());
            }
        } catch (IOException e) {
            System.out.println("This should not have happened.");
        }
        return out.toByteArray();
    }

    public static byte[] concatBigIntegerListsToByteArray(List<BigInteger> a, List<BigInteger> b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (BigInteger coeff : a) {
                out.write(coeff.toByteArray());
            }
            for (BigInteger coeff : b) {
                out.write(coeff.toByteArray());
            }
        } catch (IOException e) {
            System.out.println("This should not have happened.");
        }
        return out.toByteArray();
    }

    public static byte[] concatenateTwoByteArraysAndHash(Engine engine, byte[] a, byte[] b) {
        byte[] input = new byte[a.length + b.length];
        System.arraycopy(a, 0, input, 0, a.length);
        System.arraycopy(b, 0, input, a.length, b.length);
        byte[] hashed = new byte[32];
        engine.hash(hashed, input);
        return hashed;
    }

    public static byte[] concatenateThreeByteArraysAndHash(Engine engine, byte[] a, byte[] b, byte[] c) {
        byte[] input = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, input, 0, a.length);
        System.arraycopy(b, 0, input, a.length, b.length);
        System.arraycopy(c, 0, input, a.length + b.length, c.length);
        byte[] hashed = new byte[32];
        engine.hash(hashed, input);
        return hashed;
    }

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
