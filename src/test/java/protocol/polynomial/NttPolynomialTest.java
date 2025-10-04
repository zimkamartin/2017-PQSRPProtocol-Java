package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NttPolynomialTest {

    private static final int NUMBEROFROUNDS = 111;

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    // Be sure that N < Q.
    private List<BigInteger> generateIncrementingList() {
        List<BigInteger> result = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            result.add(BigInteger.valueOf(i));
        }
        return result;
    }

    @Test
    public void ConvertToNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            List<BigInteger> nttCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
            NttPolynomial nttPolynomial = NttPolynomial.fromClassicalCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            assertEquals(nttCoefficients, nttPolynomial.getCoefficients());
        }
    }

    @Test
    public void addPolys() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial a = NttPolynomial.fromNttCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            List<BigInteger> bCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));
            NttPolynomial b = NttPolynomial.fromNttCoefficients(bCoefficients, new PolynomialConfig(N, Q));
            NttPolynomial output = a.add(b);
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(6), BigInteger.valueOf(13), BigInteger.valueOf(2));
            assertEquals(expectedCoefficients, output.getCoefficients());
        }
    }

    @Test
    public void subtractPolys() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial a = NttPolynomial.fromNttCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            List<BigInteger> bCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));
            NttPolynomial b = NttPolynomial.fromNttCoefficients(bCoefficients, new PolynomialConfig(N, Q));
            NttPolynomial output = a.subtract(b);
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(13), BigInteger.valueOf(8), BigInteger.valueOf(4));
            assertEquals(expectedCoefficients, output.getCoefficients());
        }
    }

    @Test
    public void multiplyPolys() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial a = NttPolynomial.fromNttCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            List<BigInteger> bCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));
            NttPolynomial b = NttPolynomial.fromNttCoefficients(bCoefficients, new PolynomialConfig(N, Q));
            NttPolynomial output = a.multiply(b);
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(14));
            assertEquals(expectedCoefficients, output.getCoefficients());
        }
    }

    @Test
    public void constantTwoNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial constant2 = NttPolynomial.constantTwoNtt(new PolynomialConfig(N, Q));
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
            assertEquals(expectedCoefficients, constant2.getCoefficients());
        }
    }

    @Test
    public void toByteArrayWrapper() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial polynomial = NttPolynomial.fromClassicalCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            assertArrayEquals(new byte[]{0, 16, 6, 12}, polynomial.toByteArrayWrapper().getData());
        }
    }

    @Test
    public void concatWith() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            List<BigInteger> aCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16));
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(5), BigInteger.valueOf(11), BigInteger.valueOf(16), BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3));
            NttPolynomial a = NttPolynomial.fromNttCoefficients(aCoefficients, new PolynomialConfig(N, Q));
            NttPolynomial b = NttPolynomial.fromNttCoefficients(generateIncrementingList(), new  PolynomialConfig(N, Q));
            NttPolynomial output = a.concatWith(b);
            assertEquals(expectedCoefficients, output.getCoefficients());
        }
    }
}