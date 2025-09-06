package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ClassicalPolynomial extends Polynomial<ClassicalPolynomial> {

    private static List<BigInteger> convertFromNtt(List<BigInteger> nttCoeffs, List<BigInteger> zetasInverted, int n, BigInteger q) {
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

    public ClassicalPolynomial(List<BigInteger> coeffs, BigInteger q) {
        super(List.copyOf(coeffs), q);
    }

    public ClassicalPolynomial(List<BigInteger> nttCoeffs, List<BigInteger> zetasInverted, BigInteger q) {
        super(convertFromNtt(List.copyOf(nttCoeffs), List.copyOf(zetasInverted), nttCoeffs.size(), q), q);
    }

    public ClassicalPolynomial(NttPolynomial nttPoly, List<BigInteger> zetasInverted) {
        super(convertFromNtt(nttPoly.getCoeffs(), List.copyOf(zetasInverted), nttPoly.n, nttPoly.q), nttPoly.q);
    }

    @Override
    protected ClassicalPolynomial newInstance(List<BigInteger> coeffs, BigInteger q) {
        return new ClassicalPolynomial(List.copyOf(coeffs), q);
    }
}
