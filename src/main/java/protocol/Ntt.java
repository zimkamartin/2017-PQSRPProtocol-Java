package protocol;

import java.math.BigInteger;
import java.util.*;

/**
 * Represents all math operations with objects of class Polynomial. For efficiency, everything is done in NTT domain.
 * <p>
 * When an instance is constructed, arrays zetas and zetas inverted are computed.
 * Otherwise, provides just utility functions add, inverse, subtracts, multiply, create constant two polynomial.
 * NTT stuff heavily inspired by https://electricdusk.com/ntt.html
 * </p>
 */
public interface Ntt {

    /**
     * Based on n and q generates arrays Zeta and Zeta inverted. These are filled with constants used for conversion from and to Ntt representations.
     */
    void computeZetaArrays();

    /**
     * @return polynomial in Ntt form representing constant 2
     */
    List<BigInteger> generateConstantTwoPolynomialNtt();

    /**
     * @param a - polynomial in the Ntt form
     * @param b - polynomial in the Ntt form
     * @return Ntt form of the addition a + b (although work also for polynomials in classic form)
     */
    List<BigInteger> addPolys(List<BigInteger> a, List<BigInteger> b);

    /**
     * @param a - polynomial in the Ntt form
     * @param b - polynomial in the Ntt form
     * @return Ntt form of the substraction a - b (although work also for polynomials in classic form)
     */
    List<BigInteger> subtractPolys(List<BigInteger> a, List<BigInteger> b);

    /**
     * @param inputPoly - polynomial in classic form
     * @return inputPoly in Ntt form
     */
    List<BigInteger> convertToNtt(List<BigInteger> inputPoly);

    /**
     * @param a - polynomial in the Ntt form
     * @param b - polynomial in the Ntt form
     * @return Ntt form of the multiplication a * b (! works only for ntt polynomials !)
     */
    List<BigInteger> multiplyNttPolys(List<BigInteger> a, List<BigInteger> b);

    /**
     * @param inputPoly - polynomial in Ntt form
     * @return inputPoly in classic form
     */
    List<BigInteger> convertFromNtt(List<BigInteger> inputPoly);
}
