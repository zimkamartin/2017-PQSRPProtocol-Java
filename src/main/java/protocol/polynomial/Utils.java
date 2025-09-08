package protocol.polynomial;

import protocol.ByteArrayWrapper;
import protocol.ProtocolConfiguration;
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
        List<BigInteger> eCoeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        rc.generateCbdCoefficients(eCoeffs, seed.getData());
        return new NttPolynomial(new ClassicalPolynomial(List.copyOf(eCoeffs), pc), pc);
    }

    public static NttPolynomial generateRandomErrorPolyNtt(PolynomialConfig pc, RandomCustom rc) {
        byte[] eRandomSeed = new byte[34];
        rc.getRandomBytes(eRandomSeed);
        return generateRandomErrorPolyNtt(pc, rc, new ByteArrayWrapper(eRandomSeed));
    }

    public static NttPolynomial generateUniformPolyNtt(PolynomialConfig pc, RandomCustom rc, ByteArrayWrapper seed) {
        List<BigInteger> coeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        rc.generateUniformCoefficients(coeffs, seed.getData());
        return new NttPolynomial(List.copyOf(coeffs), pc);
    }

    /**
     * u = XOF(H(pi || pj))
     */
    public static NttPolynomial computeUNtt(PolynomialConfig pc, RandomCustom rc, NttPolynomial pi, NttPolynomial pj) {
        ByteArrayWrapper seed = pi.concatWith(pj).toByteArrayWrapper().hashWrapped();
        return generateUniformPolyNtt(pc, rc, seed);
    }
}
