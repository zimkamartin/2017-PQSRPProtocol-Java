package protocol.polynomial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public List<BigInteger> getCoeffs() {
        return List.copyOf(coefficients);
    }

    public int getN() {
        return n;
    }

    public BigInteger getQ() {
        return q;
    }

    public boolean isNtt() {
        return ntt;
    }

    public Polynomial defensiveCopy() {
        return new Polynomial(List.copyOf(this.coefficients), this.q, this.ntt);
    }

    private void checkCompatibility(Polynomial b) {
        if (this.n != b.getN()) {
            throw new IllegalArgumentException("Polynomials must have the same degree");
        }
        if (!this.q.equals(b.getQ())) {
            throw new IllegalArgumentException("Polynomials must use the same modulus");
        }
        if (this.ntt != b.isNtt()) {
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
            result.set(i, this.getCoeffs().get(i).add(b.getCoeffs().get(i)).mod(q));
        }
        return new Polynomial(result, q, ntt);
    }

    private Polynomial negate() {
        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.getCoeffs().get(i).negate().mod(q));
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
        if (this.n != bNtt.getN()) {
            throw new IllegalArgumentException("Polynomials must have the same degree");
        }
        if (!this.q.equals(bNtt.getQ())) {
            throw new IllegalArgumentException("Polynomials must use the same modulus");
        }
        if (!this.ntt || !bNtt.isNtt()) {
            throw new IllegalArgumentException("Both polynomials must be in NTT form");
        }

        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.getCoeffs().get(i).multiply(bNtt.getCoeffs().get(i)).mod(this.q));
        }

        return new Polynomial(result, q, true);
    }

    /**
     * @return byte array representing polynomial's coefficients
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (BigInteger coeff : this.getCoeffs()) {
                out.write(coeff.toByteArray());
            }
        } catch (IOException e) {
            System.out.println("This should not have happened.");
        }
        return out.toByteArray();
    }

    /**
     * @param b - polynomial that will be concatenated to this polynomial
     * @return new polynomial which coefficients will be concatenation this || b
     * (used later in hashing)
     */
    public Polynomial concatWith(Polynomial b) {
        checkCompatibility(b);

        List<BigInteger> result = new ArrayList<>(2 * this.n);
        result.addAll(this.getCoeffs());
        result.addAll(b.getCoeffs());

        return new Polynomial(result, this.q, this.ntt);
    }
}
