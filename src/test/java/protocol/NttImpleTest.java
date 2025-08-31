package protocol;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NttImpleTest {

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    // Be sure that n < q.
    private List<BigInteger> generateIncrementingList(int n) {
        List<BigInteger> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(BigInteger.valueOf(i));
        }
        return result;
    }

    @org.junit.jupiter.api.Test
    void computeZetaArrays() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> zetas = ntt.getZetasArray();
        List<BigInteger> zetasInverted = ntt.getZetasInvertedArray();
        List<BigInteger> zetasExpected = Arrays.asList(BigInteger.valueOf(13), BigInteger.valueOf(15), BigInteger.valueOf(9));
        List<BigInteger> zetasInvertedExpected = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(8), BigInteger.valueOf(2));
        assertEquals(zetasExpected, zetas);
        assertEquals(zetasInvertedExpected, zetasInverted);
    }

    @org.junit.jupiter.api.Test
    void generateConstantTwoPolynomialNtt() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> output = ntt.generateConstantTwoPolynomialNtt();
        List<BigInteger> expected = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void addPolys() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> a = generateIncrementingList(N);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.addPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(6), BigInteger.valueOf(13), BigInteger.valueOf(2));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void subtractPolys() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> a = generateIncrementingList(N);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.subtractPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(13), BigInteger.valueOf(8), BigInteger.valueOf(4));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void convertToNtt() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> input = generateIncrementingList(N);
        List<BigInteger> output = ntt.convertToNtt(input);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void multiplyNttPolys() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> a = generateIncrementingList(N);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.multiplyNttPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(14));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void convertFromNtt() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> input = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
        List<BigInteger> converted = ntt.convertFromNtt(input);
        List<BigInteger> expected = generateIncrementingList(N);
        assertEquals(expected, converted);
    }

    /**
     * Compute (a + b) * a - (b * b), where a is NTT version of a polynomial 2 and b is also a NTT version of some polynomial.
     * Resulting polynomial must be in classical form.
     */
    @org.junit.jupiter.api.Test
    void testEverything() {
        NttImple ntt = new NttImple(N, Q);
        List<BigInteger> aNtt = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
        List<BigInteger> b = generateIncrementingList(N);
        List<BigInteger> bNtt = ntt.convertToNtt(b);
        List<BigInteger> sumNtt = ntt.addPolys(aNtt, bNtt);
        List<BigInteger> fstMulti = ntt.multiplyNttPolys(sumNtt, aNtt);
        List<BigInteger> sndMulti = ntt.multiplyNttPolys(bNtt, bNtt);
        List<BigInteger> outputNtt = ntt.subtractPolys(fstMulti, sndMulti);
        List<BigInteger> output = ntt.convertFromNtt(outputNtt);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(14), BigInteger.valueOf(14), BigInteger.valueOf(12), BigInteger.valueOf(2));
        assertEquals(expected, output);
    }
}