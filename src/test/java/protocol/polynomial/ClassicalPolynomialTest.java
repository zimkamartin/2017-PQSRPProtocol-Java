package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The {@code ClassicalPolynomialTest} class tests the following methods in the class {@code ClassicalPolynomial}:
 * <ul>
 *     <li>constructor {@code ClassicalPolynomial(NttPolynomial, PolynomialConfig)}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class ClassicalPolynomialTest {

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
     * Tests {@code NUMBEROFROUNDS}-times constructor {@code ClassicalPolynomial(NttPolynomial, PolynomialConfig)}.
     * <p>
     * Tests that conversion from NTT domain to standard domain works. For N = 4, Q = 17, 8-th root of unity is 9
     * (computed using our method). Array zetasInverted is [4, 8, 2].
     * Polynomial [0, 16, 6, 2] in NTT domain corresponds to polynomial [0, 1, 2, 3] in standard domain.
     * </p>
     */
    @Test
    public void convertFromNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            List<BigInteger> nttCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
            NttPolynomial nttPolynomial = NttPolynomial.fromNttCoefficients(nttCoefficients, new PolynomialConfig(N, Q));
            ClassicalPolynomial classicalPolynomial = new ClassicalPolynomial(nttPolynomial, new PolynomialConfig(N, Q));
            assertEquals(generateIncrementingList(), classicalPolynomial.getCoefficients());
        }
    }
}