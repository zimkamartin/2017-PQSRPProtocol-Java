package protocol;

import java.math.BigInteger;

/**
 * The {@code ProtocolConfiguration} represents protocols configuration.
 *
 * <p>That is:</p>
 * <ul>
 *   <li>{@code n}   – {@code int}, all polynomials are of degree n-1 (they are reduced modulo (X^n + 1)).
 *                     Must be power of 2 because of Number Theoretic Transform (NTT)</li>
 *   <li>{@code q}   – {@code BigInteger}, q-1 is maximal value of polynomial coefficients (they are reduced modulo q).
 *                     Must hold q ≡ 1 mod 2n because of NTT.
 *                     Must be prime so that generator exists, and we can compute n-root of one in NTT</li>
 *   <li>{@code eta} – {@code int}, bounds error polynomial values in range [-eta, +eta], sampled from a Centered
 *                                  Binomial Distribution</li>
 * </ul>
 *
 * <p>These parameters are set at the beginning and everything in the protocol is set according to that setting.</p>
 *
 * @author Martin Zimka
 */
public class ProtocolConfiguration {

    private final int n;
    private final BigInteger q;
    private final int eta;

    public ProtocolConfiguration(int n, BigInteger q, int eta) {
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
