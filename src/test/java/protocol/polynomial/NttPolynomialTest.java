package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * The {@code NttPolynomialTest} class tests the following methods in the class {@code NttPolynomial}:
 * <ul>
 *     <li>constructor {@code NttPolynomial.fromClassicalCoefficients(List<BigInteger>, PolynomialConfig)}</li>
 *     <li>{@code add(NttPolynomial)}</li>
 *     <li>{@code subtract(NttPolynomial)}</li>
 *     <li>{@code multiply(NttPolynomial)}</li>
 *     <li>{@code constantTwoNtt(PolynomialConfig)}</li>
 *     <li>{@code toByteArrayWrapper()}</li>
 *     <li>{@code concatWith(NttPolynomial)}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class NttPolynomialTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    /**
     * Generates incrementing list of BigIntegers from value 0 to value N-1.
     *
     * <p>If this list is used as coefficients, N < Q must hold!</p>
     * @return List of BigInteger values 0, 1, ..., N-1.
     */
    private List<BigInteger> generateIncrementingList() {
        List<BigInteger> result = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            result.add(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times constructor {@code NttPolynomial.fromClassicalCoefficients(List<BigInteger>, PolynomialConfig)}.
     * <p>
     * Tests that conversion from standard domain to NTT domain works. For N = 4, Q = 17, 8-th root of unity is 9
     * (computed using our method). Array zetas is [13, 15, 9].
     * Polynomial [0, 1, 2, 3] in standard domain corresponds to polynomial [0, 16, 6, 2] in NTT domain.
     * </p>
     */
    @Test
    public void convertToNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            List<BigInteger> nttCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
            NttPolynomial nttPolynomial = NttPolynomial.fromClassicalCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            assertEquals(nttCoefficients, nttPolynomial.getCoefficients());
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code add(NttPolynomial)}.
     * <p>
     * The following should hold (every polynomial is in NTT domain): [0, 1, 2, 3] + [0, 5, 11, 16] = [0, 6, 13, 2]
     * </p>
     */
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

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code subtract(NttPolynomial)}.
     * <p>
     * The following should hold (every polynomial is in NTT domain): [0, 1, 2, 3] - [0, 5, 11, 16] = [0, 13, 8, 4]
     * </p>
     */
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

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code subtract(NttPolynomial)}.
     * <p>
     * The following should hold (every polynomial is in NTT domain): [0, 1, 2, 3] * [0, 5, 11, 16] = [0, 5, 5, 14]
     * </p>
     */
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

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code constantTwoNtt(PolynomialConfig)}.
     * <p>
     * Polynomial [2, 0, 0, 0] in standard domain corresponds to polynomial [2, 2, 2, 2] in NTT domain.
     * </p>
     */
    @Test
    public void constantTwoNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial constant2 = NttPolynomial.constantTwoNtt(new PolynomialConfig(N, Q));
            List<BigInteger> expectedCoefficients = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
            assertEquals(expectedCoefficients, constant2.getCoefficients());
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code toByteArrayWrapper()}.
     * <p>
     * Polynomial [0, 1, 2, 3] in NTT domain corresponds to the byte array {@code 0x00 0x01 0x02 0x03}.
     * </p>
     */
    @Test
    public void toByteArrayWrapper() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            NttPolynomial polynomial = NttPolynomial.fromNttCoefficients(generateIncrementingList(), new PolynomialConfig(N, Q));
            assertArrayEquals(new byte[]{0, 1, 2, 3}, polynomial.toByteArrayWrapper().getData());
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code concatWith(NttPolynomial)}.
     * <p>
     * Polynomial [0, 5, 11, 16] in NTT domain concatenated with polynomial [0, 1, 2, 3] in NTT domain corresponds
     * to polynomial [0, 5, 11, 16, 0, 1, 2, 3] in NTT domain.
     * </p>
     */
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