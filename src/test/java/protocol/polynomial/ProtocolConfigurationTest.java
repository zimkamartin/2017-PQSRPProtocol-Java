package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;

public class ProtocolConfigurationTest {

    private static final int NUMBEROFROUNDS = 111;
    private static final int MAXNEXPONENT = 10;  // huge = heap overflow

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    Random random = new Random();

    // SOURCE: ChatGPT
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
     * Tests {@code NUMBEROFROUNDS}-times that lists zetas and zetasInverted are generated correctly.
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

    @Test
    public void assertCompatibleWithCorrect() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            PolynomialConfig pc1 = generateCorrectPolynomialConfig();
            pc1.assertCompatibleWith(pc1);
            System.out.println(i);
        }
    }

    @Test
    public void assertCompatibleWithIncorrect() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            PolynomialConfig pc1 = generateCorrectPolynomialConfig();
            PolynomialConfig pc2 = generateCorrectPolynomialConfig();
            assertThrows(IllegalArgumentException.class, () -> pc1.assertCompatibleWith(pc2));
        }
    }
}