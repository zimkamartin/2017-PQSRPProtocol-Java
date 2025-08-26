package protocol;

import java.math.BigInteger;

/**
 * N and Q will be used to represent a polynomial modulo (X^N + 1) with all coefficients modulo Q.
 */
public class PublicParams {

    private int n;
    private BigInteger q;

    public PublicParams(int n,  BigInteger q) {
        this.n = n;
        this.q = q;
    }

    public int getN() {
        return n;
    }

    public BigInteger getQ() {
        return q;
    }
}
