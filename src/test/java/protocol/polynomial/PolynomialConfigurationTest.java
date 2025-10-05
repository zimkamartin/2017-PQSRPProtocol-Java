package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * The {@code PolynomialConfigurationTest} class tests the following methods in the class {@code PolynomialConfiguration}:
 * <ul>
 *     <li>{@code computeZetaArrays()}</li>
 *     <li>{@code assertCompatibleWith(PolynomialConfig)}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class PolynomialConfigurationTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;
    // Maximal exponent for N when generating correct PolynomialConfig. !Huge value equals heap overflow!
    private static final int MAXNEXPONENT = 10;

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    Random random = new Random();

    /**
     * Generates correct instance of class {@code PolynomialConfig}.
     * <p>
     * n must be power of two, q must be prime and {@code q ≡ 1 mod 2n} must hold.
     * </p>
     * <p>SOURCE: ChatGPT.</p>
     *
     * @return generated correct instance of a class {@code PolynomialConfig}.
     */
    private PolynomialConfig generateCorrectPolynomialConfig() {
        int exp = 1 + random.nextInt(MAXNEXPONENT); // exponent in [1, 10], so n in [2, 1024]
        int n = 1 << exp; // n = 2^exp

        BigInteger twoN = BigInteger.valueOf(2L * n);
        BigInteger q;

        // keep generating until we get a prime q
        do {
            // random positive multiplier
            BigInteger k = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE) + 1).add(BigInteger.ONE);
            q = k.multiply(twoN).add(BigInteger.ONE);  // q = k*(2n) + 1
        } while (!q.isProbablePrime(50));

        return new PolynomialConfig(n, q);
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code computeZetaArrays()}.
     * <p>
     * Tests that correct arrays zetas and zetasInverted are generated. For N = 4, Q = 17, 8-th root of unity is 9
     * (computed using our method). Array zetas is [13, 15, 9] and array zetasInverted is [4, 8, 2].
     * </p>
     */
    @Test
    public void computeZetaArrays() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            PolynomialConfig pc = new PolynomialConfig(N, Q);
            List<BigInteger> zetas = pc.getZetas();
            List<BigInteger> zetasInverted = pc.getZetasInverted();
            List<BigInteger> zetasExpected = Arrays.asList(BigInteger.valueOf(13), BigInteger.valueOf(15), BigInteger.valueOf(9));
            List<BigInteger> zetasInvertedExpected = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(8), BigInteger.valueOf(2));
            assertEquals(zetasExpected, zetas);
            assertEquals(zetasInvertedExpected, zetasInverted);
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that method {@code assertCompatibleWith(PolynomialConfig)} ends with success.
     * <p>
     * Tests polynomial configs with same n and q tuples. Nothing special should happen.
     * </p>
     */
    @Test
    public void assertCompatibleWithCorrect() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            PolynomialConfig pc1 = generateCorrectPolynomialConfig();
            pc1.assertCompatibleWith(pc1);
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times that method {@code assertCompatibleWith(PolynomialConfig)} ends with failure.
     * <p>
     * Tests polynomial configs with different n and q tuples. Method should throw IllegalArgumentException.
     * </p>
     */
    @Test
    public void assertCompatibleWithIncorrect() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            PolynomialConfig pc1 = generateCorrectPolynomialConfig();
            PolynomialConfig pc2 = generateCorrectPolynomialConfig();
            assertThrows(IllegalArgumentException.class, () -> pc1.assertCompatibleWith(pc2));
        }
    }
}