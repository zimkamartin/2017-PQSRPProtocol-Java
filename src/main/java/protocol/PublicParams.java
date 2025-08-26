package protocol;

import java.math.BigInteger;

/**
 * N and Q will be used to represent a polynomial modulo (X^N + 1) with all coefficients modulo Q.
 * ETA is parameter in Central binomial distribution which is used for generating error polynomials.
 */
public class PublicParams {

    /**
     * Must be power of 2 because of NTT and must not overflow data type int.
     */
    private final int n;
    /**
     * Must hold q ≡ 1 mod 2n because of NTT.
     */
    private final BigInteger q;
    /**
     * TODO Limitations will be added soon.
     */
    private final int eta;

    public PublicParams(int n,  BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
    }

    public int getN() {
        return n;
    }

    public BigInteger getQ() {
        return q;
    }

    public int getEta() {
        return eta;
    }
}
