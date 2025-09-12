package protocol.polynomial;

import protocol.ByteArrayWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Porozmyslat ci Polynomial vobec potrebujem.
// Zrusit base classu.
public abstract class Polynomial<T extends Polynomial<T>> {
// coeffs NTT != coeffs classical
    protected final List<BigInteger> coefficients;
    protected final PolynomialConfig pc;  // contains n and q

    public Polynomial(List<BigInteger> coefficients, PolynomialConfig pc) {
        this.coefficients = List.copyOf(coefficients);
        this.pc = pc;
    }

    public List<BigInteger> getCoeffs() {  // nemusi tu znovu byt copyOf, kedze je v konstruktore
        return List.copyOf(coefficients);
    }

    public PolynomialConfig getPc() {
        return pc;
    }

    protected abstract T newInstance(List<BigInteger> coeffs, PolynomialConfig pc);

    public T defensiveCopy() {  // pozriet si ci to potrebujem
        return newInstance(getCoeffs(), pc);
    }

    /**
     * @param b - polynomial that will be added to this polynomial
     * @return sum of this + b polynomials
     */
    public T add(T b) {
        pc.assertCompatibleWith(b.getPc());

        List<BigInteger> result = new java.util.ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.coefficients.get(i).add(b.coefficients.get(i)).mod(this.pc.getQ()));
        }
        return newInstance(result, pc);
    }

    protected T negate() {
        List<BigInteger> result = new java.util.ArrayList<>(this.pc.getN());
        for (int i = 0; i < this.pc.getN(); i++) {
            result.add(this.getCoeffs().get(i).negate().mod(this.pc.getQ()));
        }
        return newInstance(result, pc);
    }

    /**
     * @param b - polynomial that will be subtracted from this polynomial
     * @return subtraction of this - b polynomials
     */
    public T subtract(T b) {
        pc.assertCompatibleWith(b.getPc());

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
     * @return byte array wrapped representing polynomial's coefficients
     */
    public ByteArrayWrapper toByteArrayWrapper() {
        return new ByteArrayWrapper(toByteArray());
    }

    /**
     * @param b - polynomial that will be concatenated to this polynomial
     * @return new polynomial which coefficients will be concatenation this || b
     */
    public T concatWith(T b) {
        pc.assertCompatibleWith(b.getPc());

        List<BigInteger> result = new ArrayList<>(2 * this.pc.getN());
        result.addAll(this.getCoeffs());
        result.addAll(b.getCoeffs());

        return newInstance(result, pc);
    }
}
