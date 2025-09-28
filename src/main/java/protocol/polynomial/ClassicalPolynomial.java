package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ClassicalPolynomial} class represents a polynomial in coefficient representation
 * (also known as standard form). The polynomial is defined modulo {@code (X^N + 1)}, and all coefficients
 * are reduced modulo {@code Q}.
 *
 * <p>This class has only one attribute:</p>
 * <ul>
 *   <li>{@code coefficients} – {@code List<BigInteger>}, the list of coefficients
 *       ordered from the constant term up to the coefficient of {@code X^(N-1)}</li>
 * </ul>
 *
 * <p>Instances of this class are created by
 * {@link #ClassicalPolynomial(NttPolynomial, PolynomialConfig)}, which converts a polynomial from
 * its NTT (Number Theoretic Transform) representation back to coefficient representation using
 * the provided polynomial configuration:</p>
 * <ul>
 *   <li>{@code NttPolynomial} – the polynomial in NTT form</li>
 *   <li>{@code PolynomialConfig} – the polynomial configuration required to perform the conversion</li>
 * </ul>
 *
 * <p>Conversion from NTT inspired by
 * <a href="https://electricdusk.com/ntt.html">this blog post</a>.</p>
 * @author Martin Zimka
 */
public class ClassicalPolynomial {

    private final List<BigInteger> coefficients;

    private ClassicalPolynomial(List<BigInteger> classicalCoeffs) {
        this.coefficients = List.copyOf(classicalCoeffs);
    }

    public List<BigInteger> getCoefficients() {
        return coefficients;
    }

    /**
     * Converts NTT representation of a polynomial coefficients to a standard representation of a polynomial coefficients.
     *
     * <p>This method performs the inverse NTT using the precomputed inverted
     * 2N-th root-of-unity powers from List {@code zetasInverted} located in {@code PolynomialConfig}.
     * It follows the same layer-wise butterfly structure as method {@code convertToNtt} in class {@code NttPolynomial},
     * but in reverse order and with adjusted formulas.
     *
     * <p>At the end, each coefficient is scaled by the power of 2 modulo q.</p>
     *
     * @param nttCoeffs the polynomial coefficients in NTT domain
     * @param pc - the polynomial configuration (modulus {@code q}, degree {@code n}, and precomputed constants)
     * @return the polynomial coefficients in the standard domain
     */
    private static List<BigInteger> convertFromNtt(List<BigInteger> nttCoeffs, PolynomialConfig pc) {

        int n = pc.getN();
        BigInteger q = pc.getQ();
        List<BigInteger> zetasInverted = pc.getZetasInverted();

        List<BigInteger> coeffs = new ArrayList<>(nttCoeffs);
        int zetaIndex = zetasInverted.size() - 1;

        int numOfLayers = (int) (Math.log(n) / Math.log(2));
        for (int layer = numOfLayers - 1; layer >= 0; layer--) {
            int numOfSubpolys = (int) Math.pow(2, layer);
            int lenOfSubpoly = n / numOfSubpolys;
            for (int subpolyCounter = numOfSubpolys - 1; subpolyCounter >= 0; subpolyCounter--) {
                int polyLstIndex = subpolyCounter * lenOfSubpoly + lenOfSubpoly;
                for (int subpolyHalfIndex = polyLstIndex - 1; subpolyHalfIndex > polyLstIndex - 1 - lenOfSubpoly / 2; subpolyHalfIndex--) {
                    int subpolyIndex = subpolyHalfIndex - lenOfSubpoly / 2;
                    BigInteger oldSubpolyCoeff = coeffs.get(subpolyIndex);
                    BigInteger oldSubpolyHalfCoeff = coeffs.get(subpolyHalfIndex);
                    coeffs.set(subpolyIndex, oldSubpolyCoeff.add(oldSubpolyHalfCoeff).mod(q));
                    coeffs.set(subpolyHalfIndex, zetasInverted.get(zetaIndex).negate().multiply(oldSubpolyCoeff.subtract(oldSubpolyHalfCoeff)).mod(q));
                }
                zetaIndex--;
            }
        }

        BigInteger twoDivisor = BigInteger.TWO.modPow(BigInteger.valueOf(numOfLayers).negate(), q);
        for (int i = 0; i < n; i = i + 1) {
            coeffs.set(i, coeffs.get(i).multiply(twoDivisor).mod(q));
        }
        return coeffs;
    }

    public ClassicalPolynomial(NttPolynomial nttPolynomial, PolynomialConfig pc) {
        this(convertFromNtt(nttPolynomial.getCoefficients(), pc));
    }
}
