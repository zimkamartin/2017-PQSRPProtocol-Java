package protocol.polynomial;

import protocol.EngineImple;
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
    public static ClassicalPolynomial multiply3NttTuplesAndAddThemTogether(NttPolynomial a, NttPolynomial b, NttPolynomial c, NttPolynomial d, NttPolynomial e, NttPolynomial f, List<BigInteger> zetasInverted) {
        NttPolynomial addedFstTwo = multiply2NttTuplesAddThemTogetherNtt(a, b, c, d);
        NttPolynomial ef = e.multiply(f);
        return new ClassicalPolynomial(addedFstTwo.add(ef), List.copyOf(zetasInverted));
    }

    public static NttPolynomial generateRandomErrorPolyNtt(ProtocolConfiguration pc, RandomCustom rc, List<BigInteger> zetas, byte[] seed) {
        List<BigInteger> eCoeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        rc.generateCbdCoefficients(eCoeffs, seed.clone());
        return new NttPolynomial(List.copyOf(eCoeffs), List.copyOf(zetas), pc.getQ());
    }

    public static NttPolynomial generateRandomErrorPolyNtt(ProtocolConfiguration pc, RandomCustom rc, List<BigInteger> zetas) {
        byte[] eRandomSeed = new byte[34];
        rc.getRandomBytes(eRandomSeed);
        return generateRandomErrorPolyNtt(pc, rc, List.copyOf(zetas), eRandomSeed);
    }

    public static NttPolynomial generateUniformPolyNtt(ProtocolConfiguration pc, RandomCustom rc, byte[] seed) {
        List<BigInteger> coeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        rc.generateUniformCoefficients(coeffs, seed.clone());
        return new NttPolynomial(List.copyOf(coeffs), pc.getQ());
    }

    /**
     * u = XOF(H(pi || pj))
     */
    public static NttPolynomial computeUNtt(ProtocolConfiguration pc, EngineImple engine, RandomCustom rc, NttPolynomial pi, NttPolynomial pj) {
        byte[] seed = new byte[32];
        engine.hash(seed, pi.concatWith(pj).toByteArray());
        return generateUniformPolyNtt(pc, rc, seed);
    }
}
