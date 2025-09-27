package protocol.polynomial;

import protocol.ByteArrayWrapper;
import protocol.random.RandomCustom;

import java.math.BigInteger;
import java.util.List;

/** * The {@code Utils} class is utility class for polynomials in mostly NTT form.
 * <p>All math related single-polynomial operations are in the class NttPolynomial. Here are:<\p>
 *
 * <ul>
 *   <li>{@link #multiply2NttTuplesAddThemTogetherNtt(NttPolynomial, NttPolynomial, NttPolynomial, NttPolynomial)} –
 *       multiplies two pairs of NTT polynomials and adds the results</li>
 *   <li>{@link #multiply3NttTuplesAndAddThemTogether(PolynomialConfig, NttPolynomial, NttPolynomial,
 *       NttPolynomial, NttPolynomial, NttPolynomial, NttPolynomial)} –
 *       multiplies three pairs of NTT polynomials, adds the results and converts to standard form</li>
 *   <li>{@link #generateRandomErrorPolyNtt(PolynomialConfig, RandomCustom, ByteArrayWrapper)} –
 *       generates a random error polynomial (by Center Binomial Distribution) in NTT form using the given seed</li>
 *   <li>{@link #generateRandomErrorPolyNtt(PolynomialConfig, RandomCustom)} –
 *       generates a random error polynomial by creating a random seed and
 *       delegating to the previous method</li>
 *   <li>{@link #generateUniformPolyNtt(PolynomialConfig, RandomCustom, ByteArrayWrapper)} –
 *       generates a random polynomial (by Uniform distribution) in NTT form</li>
 *   <li>{@link #computeUNtt(PolynomialConfig, RandomCustom, NttPolynomial, NttPolynomial)} –
 *       computes the parameter {@code u} as defined in the protocol</li>
 * </ul>
 *
 * @author Martin Zimka
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
        List<BigInteger> eCoeffs = rc.generateCbdCoefficients(pc.getN(), seed.getData().clone());
        return NttPolynomial.fromClassicalCoefficients(eCoeffs, pc);
    }

    public static NttPolynomial generateRandomErrorPolyNtt(PolynomialConfig pc, RandomCustom rc) {
        return generateRandomErrorPolyNtt(pc, rc, new ByteArrayWrapper(rc, 34));
    }

    public static NttPolynomial generateUniformPolyNtt(PolynomialConfig pc, RandomCustom rc, ByteArrayWrapper seed) {
        List<BigInteger> coeffs = rc.generateUniformCoefficients(pc.getN(), seed.getData());
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
