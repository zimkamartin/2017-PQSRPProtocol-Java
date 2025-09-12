package protocol.random;

import java.math.BigInteger;
import java.util.List;

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
