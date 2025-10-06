package protocol.random;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The {@code RandomCustomImpleTest} class tests the following methods in the class {@code RandomCustomImple}:
 * <ul>
 *     <li>{@code generateUniformCoefficients(byte[]}</li>
 *     <li>{@code generateCbdCoefficients(byte[]}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class RandomCustomImpleTest {

    private static final int NUMBEROFROUNDS = 111;
    private static final int ZEROBYTESSEEDLEN = 32;  // length of a zero bytes seed for xof and prf

    private static final int N = 5;
    private static final BigInteger Q = BigInteger.valueOf(17);
    private static final int ETA = 3;

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code generateUniformCoefficients(byte[])}.
     * <p>
     * In this test setup, maximal value of a valid coefficient is {@code 16}. For each coefficient 1 byte is taken,
     * masked to keep the lowest 5 bits, and accepted if the resulting value is less than or equal to 16.
     * </p>
     * <p>The decimal representation of the first bytes produced by {@code SHAKE128} (seeded with 32 zero bytes) is:</p>
     * <pre>= [      36,      -89,      -54,       75,      117,      -29,     -119,     -115,       79,       18,      -25].</pre>
     * <p>The same sequence in binary representation is:</p>
     * <pre>= [00100100, 10100111, 11001010, 01001011, 01110101, 11100011, 10001001, 10001101, 01001111, 00010010, 11100110].</pre>
     * <p>After applying the bitmask, the resulting decimal values are:</\p>
     * <pre>= [4       , 7       , 10      ,       11,       21,        3,        9,       13,       15,       18,        6].</pre>
     * <p>Valid coefficients extracted from this sequence are:</p>
     * <pre>= [4, 7, 10, 11, 3].</pre>
     */
    @Test
    public void generateUniformCoefficientsTest() {

        List<BigInteger> expectedResult = Arrays.asList(BigInteger.valueOf(4), BigInteger.valueOf(7), BigInteger.valueOf(10), BigInteger.valueOf(11), BigInteger.valueOf(3));

        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            RandomCustom rc = new RandomCustomImple(N, Q, ETA);
            List<BigInteger> result = rc.generateUniformCoefficients(new byte[ZEROBYTESSEEDLEN]);
            assertEquals(expectedResult, result);
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code generateCbdCoefficients(byte[])}.
     * <p>The decimal representation of the first bytes produced by {@code SHAKE128} (seeded with 32 zero bytes) is:</p>
     * <pre>= [     -11,     -105,      124,     -126].</pre>
     * <p>The same sequence in binary representation is:</p>
     * <pre>= [11110101, 10010111, 01111100, 10000010].</pre>
     * <p>Groups of 3 bits look like:</p>
     * <pre>= [111, 101, 011, 001, 011, 101, 111, 100, 100, 000, 10..].</pre>
     * <p>Resulting CBD sequence is the following:</p>
     * <pre>= [1, 1, 0, 2, 1].</pre>
     * <p>Sadly it does not look like CBD, however in my opinion the algorithm is correct.</p>
     */
    @Test
    public void generateCbdCoefficientsTest() {

        List<BigInteger> expectedResult = Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(1), BigInteger.valueOf(0), BigInteger.valueOf(2), BigInteger.valueOf(1));

        for (int i = 0; i < NUMBEROFROUNDS; i++) {

            RandomCustom rc = new RandomCustomImple(N, Q, ETA);
            List<BigInteger> result = rc.generateCbdCoefficients(new byte[ZEROBYTESSEEDLEN]);
            assertEquals(expectedResult, result);
        }
    }
}
