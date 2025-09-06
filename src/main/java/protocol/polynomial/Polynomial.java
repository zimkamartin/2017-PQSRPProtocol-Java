package protocol.polynomial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Polynomial<T extends Polynomial<T>> {

    protected final List<BigInteger> coefficients;
    protected final int n;  // number of coefficients
    protected final BigInteger q;  // modulus

    public Polynomial(List<BigInteger> coefficients, BigInteger q) {
        this.coefficients = List.copyOf(coefficients);
        this.n = coefficients.size();
        this.q = q;
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

    protected abstract T newInstance(List<BigInteger> coeffs, BigInteger q);

    public T defensiveCopy() {
        return newInstance(this.getCoeffs(), q);
    }

    private void checkCompatibility(T b) {
        if (this.n != b.getN()) {
            throw new IllegalArgumentException("Polynomials must have the same degree");
        }
        if (!this.q.equals(b.getQ())) {
            throw new IllegalArgumentException("Polynomials must use the same modulus");
        }
    }

    /**
     * @param b - polynomial that will be added to this polynomial
     * @return sum of this + b polynomials
     */
    public T add(T b) {
        checkCompatibility(b);

        List<BigInteger> result = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(this.coefficients.get(i).add(b.coefficients.get(i)).mod(q));
        }
        return newInstance(result, q);
    }

    protected T negate() {
        List<BigInteger> result = new ArrayList<>(Collections.nCopies(n, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            result.set(i, this.getCoeffs().get(i).negate().mod(q));
        }
        return newInstance(result, q);
    }

    /**
     * @param b - polynomial that will be subtracted from this polynomial
     * @return subtraction of this - b polynomials
     */
    public T subtract(T b) {
        checkCompatibility(b);

        return this.add(b.negate());
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
    public T concatWith(T b) {
        checkCompatibility(b);

        List<BigInteger> result = new ArrayList<>(2 * this.n);
        result.addAll(this.getCoeffs());
        result.addAll(b.getCoeffs());

        return newInstance(result, this.q);
    }
}
