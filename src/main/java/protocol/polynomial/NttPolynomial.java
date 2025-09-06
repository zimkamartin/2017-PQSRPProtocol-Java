package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NttPolynomial extends Polynomial<NttPolynomial> {

    private static List<BigInteger> convertToNtt(List<BigInteger> coeffs, List<BigInteger> zetas, int n, BigInteger q) {
        List<BigInteger> nttCoeffs = new ArrayList<>(List.copyOf(coeffs));
        int zetaIndex = 0;

        int numOfLayers = (int) (Math.log(n) / Math.log(2));
        for (int layer = 0; layer < numOfLayers; layer++) {
            int numOfSubpolys = (int) Math.pow(2, layer);
            int lenOfSubpoly = n / numOfSubpolys;
            for (int subpolyCounter = 0; subpolyCounter < numOfSubpolys; subpolyCounter++) {
                int polyLstIndex = subpolyCounter * lenOfSubpoly - 1;
                for (int subpolyIndex = polyLstIndex + 1; subpolyIndex < polyLstIndex + 1 + lenOfSubpoly / 2; subpolyIndex++) {
                    int subpolyHalfIndex = subpolyIndex + lenOfSubpoly / 2;
                    BigInteger oldSubpolyCoeff = nttCoeffs.get(subpolyIndex);
                    BigInteger oldSubpolyHalfCoeff = nttCoeffs.get(subpolyHalfIndex);
                    nttCoeffs.set(subpolyIndex, (oldSubpolyCoeff.subtract(zetas.get(zetaIndex).multiply(oldSubpolyHalfCoeff))).mod(q));
                    nttCoeffs.set(subpolyHalfIndex, (oldSubpolyCoeff.add(zetas.get(zetaIndex).multiply(oldSubpolyHalfCoeff))).mod(q));
                }
                zetaIndex++;
            }
        }

        return nttCoeffs;
    }

    public NttPolynomial(List<BigInteger> nttCoeffs, BigInteger q) {
        super(List.copyOf(nttCoeffs), q);
    }

    public NttPolynomial(List<BigInteger> coeffs, List<BigInteger> zetas, BigInteger q) {
        super(convertToNtt(List.copyOf(coeffs), List.copyOf(zetas), coeffs.size(), q),q);
    }

    public NttPolynomial(NttPolynomial classicalPoly, List<BigInteger> zetas) {
        super(convertToNtt(classicalPoly.getCoeffs(), List.copyOf(zetas), classicalPoly.n, classicalPoly.q), classicalPoly.q);
    }

    @Override
    protected NttPolynomial newInstance(List<BigInteger> nttCoeffs, BigInteger q) {
        return new NttPolynomial(List.copyOf(nttCoeffs), q);
    }

    /**
     * @param b - NTT polynomial
     * @return ntt form of a * b
     */
    public NttPolynomial multiply(NttPolynomial b) {

        List<BigInteger> result = new ArrayList<>(Collections.nCopies(this.getN(), BigInteger.ZERO));
        for (int i = 0; i < this.getN(); i++) {
            result.set(i, this.getCoeffs().get(i).multiply(b.getCoeffs().get(i)).mod(q));
        }

        return new NttPolynomial(result, q);
    }

    /**
     * @return polynomial in Ntt form representing constant 2
     */
    public static NttPolynomial constantTwoNtt(int n, BigInteger q) {
        List<BigInteger> nttCoeffs = Collections.nCopies(n, BigInteger.TWO);
        return new NttPolynomial(nttCoeffs, q);
    }
}
