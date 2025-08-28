package protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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

    public static byte[] hashConvertIntegerListToByteArray(int n, Engine engine, List<Integer> inp) {
        byte[] hash = new byte[32];
        byte[] inpByteArray = new byte[n];
        for (int i = 0; i < n; i++) {
            inpByteArray[i] = inp.get(i).byteValue();
        }
        engine.hash(hash, inpByteArray);
        return hash;
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
        List<BigInteger> e = new ArrayList<>(Collections.nCopies(pp.getN(), null));
        byte[] eRandomSeed = new byte[34];
        engine.getRandomBytes(eRandomSeed);
        getEtaNoise(pp, mlkem, engine, e, eRandomSeed);
        return ntt.convertFromNtt(e);
    }

    public static List<BigInteger> computeUNtt(Engine engine, Mlkem mlkem, int n, List<BigInteger> piNtt, List<BigInteger> pjNtt) {
        byte[] hash = new byte[32];
        engine.hash(hash, Utils.concatBigIntegerListsToByteArray(piNtt, pjNtt));
        List<BigInteger> uNtt = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformPolynomialNtt(engine, uNtt, hash);
        return uNtt;
    }

    /**
     * @return ab + cd, where each polynomial is in NTT form
     */
    public static List<BigInteger> multiply2NttTuplesAndAddThemTogetherNtt(Ntt ntt, List<BigInteger> aNtt, List<BigInteger> bNtt, List<BigInteger> cNtt, List<BigInteger> dNtt) {
        List<BigInteger> abNtt = ntt.multiplyNttPolys(aNtt, bNtt);
        List<BigInteger> cdNtt = ntt.multiplyNttPolys(cNtt, dNtt);
        return ntt.addPolys(abNtt, cdNtt);
    }

    /**
     * @return convertFromNtt(ab + cd + ef), where each polynomial is in NTT form
     */
    public static List<BigInteger> multiply3NttTuplesAndAddThemTogether(Ntt ntt, List<BigInteger> aNtt, List<BigInteger> bNtt, List<BigInteger> cNtt, List<BigInteger> dNtt, List<BigInteger> eNtt, List<BigInteger> fNtt) {
        List<BigInteger> addedFstTwoNtt = multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, bNtt, cNtt, dNtt);
        List<BigInteger> efNtt = ntt.multiplyNttPolys(eNtt, fNtt);
        List<BigInteger> resultNtt = ntt.addPolys(addedFstTwoNtt, efNtt);
        return ntt.convertFromNtt(resultNtt);
    }
}
