package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ClassicalPolynomial extends Polynomial<ClassicalPolynomial> {

    private static List<BigInteger> convertFromNtt(List<BigInteger> nttCoeffs, PolynomialConfig pc) {

        int n = pc.getN();
        BigInteger q = pc.getQ();
        List<BigInteger> zetasInverted = List.copyOf(pc.getZetasInverted());

        List<BigInteger> coeffs = new ArrayList<>(List.copyOf(nttCoeffs));
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

    private ClassicalPolynomial(List<BigInteger> classicalCoeffs, PolynomialConfig pc) {
        super(List.copyOf(classicalCoeffs), pc);
    }

    public static ClassicalPolynomial fromClassicalCoefficients(List<BigInteger> classicalCoeffs, PolynomialConfig pc) {
        return new ClassicalPolynomial(classicalCoeffs, pc);
    }

    public ClassicalPolynomial(NttPolynomial nttPoly, PolynomialConfig pc) {
        super(convertFromNtt(nttPoly.getCoeffs(), pc), pc);
    }

    @Override
    protected ClassicalPolynomial newInstance(List<BigInteger> classicalCoeffs, PolynomialConfig pc) {
        return ClassicalPolynomial.fromClassicalCoefficients(classicalCoeffs, pc);
    }
}
