package protocol.random;

import java.math.BigInteger;
import java.util.List;

public interface RandomCustom {

    /**
     * @param bytes - byte array which will be filed by random bytes
     */
    void getRandomBytes(byte[] bytes);

    /**
     * @param bound - upper bound of the interval
     * @return random int value between 0 (inclusive) and the specified value bound (exclusive)
     */
    int getRandomBit(int bound);

    /**
     * @param out - list that will have coefficients sampled by uniform distribution. List can represent classical or ntt polynomial
     * @param seed - seed for generating uniform data
     */
    void generateUniformCoefficients(List<BigInteger> out, byte[] seed);

    /**
     * @param out - list that will have coefficients sampled by centered binomial distribution. List can represent classical polynomial.
     * @param seed - seed for generating uniform data later used to create centered binomial distribution (with param. eta)
     */
    void generateCbdCoefficients(List<BigInteger> out, byte[] seed);
}
