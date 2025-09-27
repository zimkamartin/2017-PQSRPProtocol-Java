package protocol.polynomial;

import protocol.ByteArrayWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code NttPolynomial} class represents a polynomial in the NTT (Number Theoretic Transform) domain.
 * The polynomial is defined modulo {@code (X^N + 1)}, and all coefficients are reduced modulo {@code Q}.
 *
 * <p>Polynomial represented in NTT form is just classical polynomial modulo (X^N + 1) represented as N remainders
 * of polynomial modulo N polynomials of degree 1 of a form (X ± <integer>). Such representation uniquely
 * represents one and only polynomial, thanks to the Chinese remainder theorem.</p>
 *
 * <p>This class has the following attributes:</p>
 * <ul>
 *   <li>{@code coefficients} – {@code List<BigInteger>}, the list of {@code N} remainders
 *                              representing the polynomial in NTT form</li>
 *   <li>{@code pc}           – {@code PolynomialConfig}, the configuration required for conversions
 *                              and operations in the NTT domain</li>
 * </ul>
 *
 * <p>Instances of this class are created using one of the following:</p>
 * <ul>
 *   <li>{@link #fromNttCoefficients(List, PolynomialConfig)} – creates an instance directly
 *       from NTT coefficients and a configuration</li>
 *   <li>{@link #fromClassicalCoefficients(List, PolynomialConfig)} – creates an instance
 *       by converting coefficient representation into NTT form, using the given configuration</li>
 * </ul>
 *
 * <p>This class provides arithmetic over NTT polynomials:</p>
 * <ul>
 *   <li>{@link #add(NttPolynomial)}</li>
 *   <li>{@link #subtract(NttPolynomial)}</li>
 *   <li>{@link #multiply(NttPolynomial)}</li>
 * </ul>
 *
 * <p>Additional methods include:</p>
 * <ul>
 *   <li>{@link #constantTwoNtt(PolynomialConfig)} – generates the NTT representation
 *                                                   of the constant polynomial {@code 2}</li>
 *   <li>{@link #toByteArrayWrapper()}             – returns a {@code ByteArrayWrapper} representation</li>
 *   <li>{@link #concatWith(NttPolynomial)}        – returns the NTT representation of {@code this * X^N + argument}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class NttPolynomial {

    private final List<BigInteger> coefficients;
    private final PolynomialConfig pc;

    private NttPolynomial(List<BigInteger> nttCoeffs, PolynomialConfig pc) {
        this.coefficients = List.copyOf(nttCoeffs);
        this.pc = pc;
    }

    List<BigInteger> getCoefficients() {
        return coefficients;
    }

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

    public static NttPolynomial fromNttCoefficients(List<BigInteger> nttCoeffs, PolynomialConfig pc) {
        return new NttPolynomial(nttCoeffs, pc);
    }

    public static NttPolynomial fromClassicalCoefficients(List<BigInteger> classicalCoeffs, PolynomialConfig pc) {
        return new NttPolynomial(convertToNtt(classicalCoeffs, pc), pc);
    }

    /**
     * @param b - ntt polynomial that will be added to this ntt polynomial
     * @return sum of this + b ntt polynomials
     */
    public NttPolynomial add(NttPolynomial b) {
        pc.assertCompatibleWith(b.pc);

        List<BigInteger> result = new java.util.ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.coefficients.get(i).add(b.coefficients.get(i)).mod(this.pc.getQ()));
        }
        return NttPolynomial.fromNttCoefficients(result, pc);
    }

    private NttPolynomial negate() {
        List<BigInteger> result = new java.util.ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.coefficients.get(i).negate().mod(this.pc.getQ()));
        }
        return NttPolynomial.fromNttCoefficients(result, pc);
    }

    /**
     * @param b - ntt polynomial that will be subtracted from this ntt polynomial
     * @return subtraction of this - b ntt polynomials
     */
    public NttPolynomial subtract(NttPolynomial b) {
        pc.assertCompatibleWith(b.pc);

        return this.add(b.negate());
    }

    /**
     * @param b - NTT polynomial
     * @return ntt form of a * b
     */
    public NttPolynomial multiply(NttPolynomial b) {
        pc.assertCompatibleWith(b.pc);

        List<BigInteger> result = new ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.coefficients.get(i).multiply(b.coefficients.get(i)).mod(this.pc.getQ()));
        }

        return NttPolynomial.fromNttCoefficients(result, pc);
    }

    /**
     * @return polynomial in Ntt form representing constant 2
     */
    public static NttPolynomial constantTwoNtt(PolynomialConfig pc) {
        List<BigInteger> nttCoeffs = Collections.nCopies(pc.getN(), BigInteger.TWO);
        return NttPolynomial.fromNttCoefficients(nttCoeffs, pc);
    }

    /**
     * @return byte array wrapped representing ntt polynomial's coefficients
     */
    public ByteArrayWrapper toByteArrayWrapper() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (BigInteger coeff : this.coefficients) {
                out.write(coeff.toByteArray());
            }
        } catch (IOException e) {
            System.out.println("This should not have happened.");
        }
        return new ByteArrayWrapper(out.toByteArray());
    }

    /**
     * @param b - ntt polynomial that will be concatenated to this ntt polynomial
     * @return new ntt polynomial which coefficients will be concatenation this || b
     */
    public NttPolynomial concatWith(NttPolynomial b) {
        pc.assertCompatibleWith(b.pc);

        List<BigInteger> result = new ArrayList<>(2 * this.pc.getN());
        result.addAll(this.coefficients);
        result.addAll(b.coefficients);

        return NttPolynomial.fromNttCoefficients(result, pc);
    }
}
