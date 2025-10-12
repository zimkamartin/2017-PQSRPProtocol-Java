package protocol.random;

import java.math.BigInteger;
import java.util.List;

/**
 * The {@code RandomCustom} interface defines an abstraction for all random operations
 * used in the protocol.
 *
 * <p>It defines the following methods:</p>
 * <ul>
 *   <li>{@link #getRandomBytes(int)}                 – generates an array of {@code n} random bytes</li>
 *   <li>{@link #getRandomInt(int)}                   – returns a random integer in the range
 *                                                      {@code [0, bound)}</li>
 *   <li>{@link #generateUniformCoefficients(byte[])} – produces n Rq coefficients sampled uniformly,
 *                                                      suitable for classical or Number Theoretic Transform
 *                                                      polynomials</li>
 *   <li>{@link #generateCbdCoefficients(byte[])}     – produces n Rq coefficients (in standard form)
 *                                                      sampled from Centred Binomial Distribution (parametrized by eta)</li>
 * </ul>
 * <p>
 * Parameters {@code n}, {@code q}, and {@code eta} for the last two methods are attributes of the implementing class.
 * </p>
 *
 * @author Martin Zimka
 */
public interface RandomCustom {

    /**
     * Generates n random bytes.
     * 
     * @param n - number of random bytes that should be returned
     * @return n random bytes.
     */
    byte[] getRandomBytes(int n);

    /**
     * Generates random Integer from interval [0; bound).
     * 
     * @param bound - upper (exclusive) bound of the interval
     * @return random int value between 0 (inclusive) and the specified value bound (exclusive)
     */
    int getRandomInt(int bound);

    /**
     * Samples a list of uniformly distributed Rq BigInteger values derived from the given seed.
     *
     * <p>This list can represent list of coefficients in standard or in NTT domain.</p>
     *
     * @param seed - seed for generating uniform data
     * @return list of Uniformly distributed BigInteger values derived from the given seed.
     */
    List<BigInteger> generateUniformCoefficients(byte[] seed);

    /**
     * Samples Rq representation of a list of BigInteger values form interval [-eta; +eta] using the Centered Binomial
     * Distribution, derived from the given seed.
     *
     * <p>This list can represent list of coefficients in standard domain.</p>
     *
     * @param seed - seed for generating Centered Binomial Distribution data
     * @return a list of BigInteger values from CBD, deterministically derived from the given seed.
     */
    List<BigInteger> generateCbdCoefficients(byte[] seed);
}
