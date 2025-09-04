package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polynomial {

    private final List<BigInteger> coefficients;
    private final int n;  // number of coefficients
    private final BigInteger q;  // modulus
    private final boolean ntt;  // true = NTT form, false = coefficient form

    public Polynomial(List<BigInteger> coefficients, BigInteger q, boolean ntt) {
        this.coefficients = List.copyOf(coefficients);
        this.n = coefficients.size();
        this.q = q;
        this.ntt = ntt;
    }

    public List<BigInteger> coefficients() {
        return coefficients;
    }

    private void checkCompatibility(Polynomial b) {
        if (this.n != b.n) {
            throw new IllegalArgumentException("Polynomials must have the same degree");
        }
        if (!this.q.equals(b.q)) {
            throw new IllegalArgumentException("Polynomials must use the same modulus");
        }
        if (this.ntt != b.ntt) {
            throw new IllegalArgumentException("Both polynomials must be in the same form (NTT or classic)");
        }
    }

    /**
     * @return polynomial in Ntt form representing constant 2
     */
    public static Polynomial constantTwoNtt(int n, BigInteger q) {
        List<BigInteger> coeffs = Collections.nCopies(n, BigInteger.TWO);
        return new Polynomial(coeffs, q, true);
    }

    /**
     * @param b - polynomial that will be added to this polynomial
     * @return sum of this + b polynomials
     */
    public Polynomial add(Polynomial b) {
        checkCompatibility(b);

        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.coefficients().get(i).add(b.coefficients().get(i)).mod(q));
        }
        return new Polynomial(result, q, this.ntt);
    }

    private Polynomial negate() {
        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.coefficients().get(i).negate().mod(q));
        }
        return new Polynomial(result, q, ntt);
    }

    /**
     * @param b - polynomial that will be subtracted from this polynomial
     * @return subtraction of this - b polynomials
     */
    public Polynomial subtract(Polynomial b) {
        checkCompatibility(b);

        return this.add(b.negate());
    }

    /**
     * @param bNtt - polynomial, must be in NTT form. Will be multiplied with this polynomial (also in NTT form)
     * @return ntt form of a * b
     */
    public Polynomial multiplyNtt(Polynomial bNtt) {
        if (this.n != bNtt.n) {
            throw new IllegalArgumentException("Polynomials must have the same degree");
        }
        if (!this.q.equals(bNtt.q)) {
            throw new IllegalArgumentException("Polynomials must use the same modulus");
        }
        if (!this.ntt || !bNtt.ntt) {
            throw new IllegalArgumentException("Both polynomials must be in NTT form");
        }

        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.coefficients().get(i).multiply(bNtt.coefficients().get(i)).mod(q));
        }

        return new Polynomial(result, q, true);
    }
}
