package protocol;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NttImpleTest {

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
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> zetas = ntt.getZetasArray();
        List<BigInteger> zetasInverted = ntt.getZetasInvertedArray();
        List<BigInteger> zetasExpected = Arrays.asList(BigInteger.valueOf(13), BigInteger.valueOf(15), BigInteger.valueOf(9));
        List<BigInteger> zetasInvertedExpected = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(8), BigInteger.valueOf(2));
        assertEquals(zetasExpected, zetas);
        assertEquals(zetasInvertedExpected, zetasInverted);
    }

    @org.junit.jupiter.api.Test
    void generateConstantTwoPolynomialNtt() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> output = ntt.generateConstantTwoPolynomialNtt();
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(2), BigInteger.valueOf(2), BigInteger.valueOf(2), BigInteger.valueOf(2));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void addPolys() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> a = generateIncrementingList(n);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.addPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(6), BigInteger.valueOf(13), BigInteger.valueOf(2));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void subtractPolys() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> a = generateIncrementingList(n);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.subtractPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(13), BigInteger.valueOf(8), BigInteger.valueOf(4));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void convertToNtt() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> input = generateIncrementingList(n);
        List<BigInteger> output = ntt.convertToNtt(input);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void multiplyNttPolys() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> a = generateIncrementingList(n);
        List<BigInteger> b = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));;
        List<BigInteger> output = ntt.multiplyNttPolys(a, b);
        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(14));
        assertEquals(expected, output);
    }

    @org.junit.jupiter.api.Test
    void convertFromNtt() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> input = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
        List<BigInteger> converted = ntt.convertFromNtt(input);
        List<BigInteger> expected = generateIncrementingList(n);
        assertEquals(expected, converted);
    }

    /**
     * Compute (a + b) * a - (b * b), where a is NTT version of a polynomial 2 and b is also a NTT version of some polynomial.
     * Resulting polynomial must be in classical form.
     */
    @org.junit.jupiter.api.Test
    void testEverything() {
        int n = 4;
        BigInteger q = BigInteger.valueOf(17);
        Ntt ntt = new NttImple(n, q);
        List<BigInteger> aNtt = ntt.generateConstantTwoPolynomialNtt();
        List<BigInteger> b = generateIncrementingList(n);
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