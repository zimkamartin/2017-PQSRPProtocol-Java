package protocol.random;

import java.math.BigInteger;
import java.util.List;

/**
 * The {@code RandomCustom} interface defines an abstraction for all random operations
 * used in the protocol.
 *
 * <p>It defines the following methods:</p>
 * <ul>
 *   <li>{@link #getRandomBytes(int)}                      – generates an array of {@code n} random bytes</li>
 *   <li>{@link #getRandomBit(int)}                        – returns a random integer in the range
 *                                                           {@code [0, bound)}</li>
 *   <li>{@link #generateUniformCoefficients(int, byte[])} – produces {@code n} coefficients sampled uniformly,
 *                                                           suitable for classical or Number Theoretic Transform
 *                                                           polynomials</li>
 *   <li>{@link #generateCbdCoefficients(int, byte[])}     – produces {@code n} coefficients (in standard form)
 *                                                           sampled from Centred Binomial Distribution</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public interface RandomCustom {

    /**
     * @param n - number of random bytes that should be returned
     */
    byte[] getRandomBytes(int n);

    /**
     * @param bound - upper bound of the interval
     * @return random int value between 0 (inclusive) and the specified value bound (exclusive)
     */
    int getRandomBit(int bound);

    /**
     * @param n - the size of the output list that will have coefficients sampled by uniform distribution. List can represent classical or ntt polynomial.
     * @param seed - seed for generating uniform data
     */
    List<BigInteger> generateUniformCoefficients(int n, byte[] seed);

    /**
     * @param n - the size of the output list that will have coefficients sampled by centered binomial distribution. List can represent classical polynomial.
     * @param seed - seed for generating uniform data later used to create centered binomial distribution (with param. eta)
     */
    List<BigInteger> generateCbdCoefficients(int n, byte[] seed);
}
