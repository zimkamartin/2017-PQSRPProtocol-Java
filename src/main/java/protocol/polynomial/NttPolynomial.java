package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NttPolynomial extends Polynomial<NttPolynomial> {

    private static List<BigInteger> convertToNtt(List<BigInteger> coeffs, PolynomialConfig pc) {

        int n = pc.getN();
        BigInteger q = pc.getQ();
        List<BigInteger> zetas = pc.getZetas();

        List<BigInteger> nttCoeffs = new ArrayList<>(coeffs);
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

    private NttPolynomial(List<BigInteger> nttCoeffs, PolynomialConfig pc) {
        super(nttCoeffs, pc);
    }

    public static NttPolynomial fromNttCoefficients(List<BigInteger> nttCoeffs, PolynomialConfig pc) {
        return new NttPolynomial(nttCoeffs, pc);
    }

    public static NttPolynomial fromClassicalCoefficients(List<BigInteger> classicalCoeffs, PolynomialConfig pc) {
        return new NttPolynomial(convertToNtt(classicalCoeffs, pc), pc);
    }

    @Override
    protected NttPolynomial newInstance(List<BigInteger> nttCoeffs, PolynomialConfig pc) {
        return NttPolynomial.fromNttCoefficients(nttCoeffs, pc);
    }

    /**
     * @param b - NTT polynomial
     * @return ntt form of a * b
     */
    public NttPolynomial multiply(NttPolynomial b) {
        pc.assertCompatibleWith(b.getPc());

        List<BigInteger> result = new ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.getCoeffs().get(i).multiply(b.getCoeffs().get(i)).mod(this.pc.getQ()));
        }

        return NttPolynomial.fromNttCoefficients(result, pc);
    }

    /**
     * @return polynomial in Ntt form representing constant 2
     */
    public static NttPolynomial constantTwoNtt(int n, PolynomialConfig pc) {
        List<BigInteger> nttCoeffs = Collections.nCopies(n, BigInteger.TWO);
        return NttPolynomial.fromNttCoefficients(nttCoeffs, pc);
    }
}
