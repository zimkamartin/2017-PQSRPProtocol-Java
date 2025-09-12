package protocol.polynomial;

import protocol.ByteArrayWrapper;
import protocol.random.RandomCustom;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utils class for polynomials. All math related single-polynomial operations are in the class Polynomial,
 * remaining operations (NON-math single-polynomial or more than single-polynomial) are here.
 */
public final class Utils {

    private Utils() {}

    /**
     * @return ab + cd, where each polynomial is in NTT form
     */
    public static NttPolynomial multiply2NttTuplesAddThemTogetherNtt(NttPolynomial a, NttPolynomial b, NttPolynomial c, NttPolynomial d) {
        NttPolynomial ab = a.multiply(b);
        NttPolynomial cd = c.multiply(d);
        return ab.add(cd);
    }

    /**
     * @return fromNtt(ab + cd + ef), where each polynomial is in NTT form
     */
    public static ClassicalPolynomial multiply3NttTuplesAndAddThemTogether(PolynomialConfig pc, NttPolynomial a, NttPolynomial b, NttPolynomial c, NttPolynomial d, NttPolynomial e, NttPolynomial f) {
        NttPolynomial addedFstTwo = multiply2NttTuplesAddThemTogetherNtt(a, b, c, d);
        NttPolynomial ef = e.multiply(f);
        return new ClassicalPolynomial(addedFstTwo.add(ef), pc);
    }

    public static NttPolynomial generateRandomErrorPolyNtt(PolynomialConfig pc, RandomCustom rc, ByteArrayWrapper seed) {
        List<BigInteger> eCoeffs = rc.generateCbdCoefficients(pc.getN(), seed.getData());
        return NttPolynomial.fromClassicalCoefficients(eCoeffs, pc);
    }

    public static NttPolynomial generateRandomErrorPolyNtt(PolynomialConfig pc, RandomCustom rc) {
        return generateRandomErrorPolyNtt(pc, rc, new ByteArrayWrapper(rc, 34));
    }

    public static NttPolynomial generateUniformPolyNtt(PolynomialConfig pc, RandomCustom rc, ByteArrayWrapper seed) {
        List<BigInteger> coeffs = rc.generateUniformCoefficients(pc.getN(), seed.getData());  // ma dostat n na vstupe a vratit list koeficientov (fcie dostanu na vstupe nieco co nemodifikuju a vratia novu vec cez navratovu hodnotu)
        return NttPolynomial.fromNttCoefficients(coeffs, pc);
    }

    /**
     * u = XOF(H(pi || pj))
     */
    public static NttPolynomial computeUNtt(PolynomialConfig pc, RandomCustom rc, NttPolynomial pi, NttPolynomial pj) {
        ByteArrayWrapper seed = pi.concatWith(pj).toByteArrayWrapper().hashWrapped();
        return generateUniformPolyNtt(pc, rc, seed);
    }
}
