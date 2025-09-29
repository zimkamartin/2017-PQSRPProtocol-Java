package protocol.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@code PolynomialConfig} class defines the configuration parameters for polynomials used in NTT-based operations.
 *
 * <p>It consists of the following attributes:</p>
 * <ul>
 *   <li>{@code n}              – {@code int}, n-1 is the degree of the polynomial
 *                                (coefficients are reduced modulo {@code X^n + 1})</li>
 *   <li>{@code q}              – {@code BigInteger}, q-1 is the maximal value of polynomial coefficients
 *                                (all coefficients are reduced modulo {@code q})</li>
 *   <li>{@code zetas}          – {@code List<BigInteger>}, constants used for conversion
 *                                from coefficient representation to NTT representation</li>
 *   <li>{@code zetasInverted}  – {@code List<BigInteger>}, constants used for conversion
 *                                from NTT representation back to coefficient representation</li>
 *   <li>{@code moduloPolyTree} – internal structure used only to compute
 *                                {@code zetas} and {@code zetasInverted}</li>
 * </ul>
 *
 * <p>Apart from standard getters (although package-private), this class provides the method
 * {@link #assertCompatibleWith(PolynomialConfig)}.
 * It is used at the beginning of polynomial operations to verify that
 * both polynomials share the same configuration.</p>
 *
 * Zetas and zetasInverted generation inspired by
 * <a href="https://electricdusk.com/ntt.html">this blog post</a>.</p>
 * @author Martin Zimka
 */
public class PolynomialConfig {

    private final int n;
    private final BigInteger q;

    private final List<BigInteger> zetas;
    private final List<BigInteger> zetasInverted;

    private final List<List<ModuloPoly>> moduloPolyTree;

    /**
     * Computes the tree of modulo polynomials, from the 2 roots at degree {@code X^(N/2)}
     * down to the leaves at degree {@code X^1}.
     *
     * <p>Each polynomial of the form:</p>
     * <ul>
     *   <li>{@code (X^powerX + (ζ_indexZeta)^powerZeta)} is divided into
     *       {@code (X^(powerX/2) + (ζ_(2*indexZeta))^(powerZeta + indexZeta/2))} and
     *       {@code (X^(powerX/2) - (ζ_(2*indexZeta))^(powerZeta + indexZeta/2))}</li>
     *   <li>{@code (X^powerX - (ζ_indexZeta)^powerZeta)} is divided into
     *       {@code (X^(powerX/2) + (ζ_(2*indexZeta))^powerZeta)} and
     *       {@code (X^(powerX/2) - (ζ_(2*indexZeta))^powerZeta)}</li>
     * </ul>
     *
     * <p>The resulting polynomials are stored in the class attribute {@code moduloPolyTree}.</p>
     */
    private void computeNttTree() {
        int powerX = n / 2;
        BigInteger indexZeta = BigInteger.valueOf(4);
        List<ModuloPoly> fstLayer = new ArrayList<>(2);
        fstLayer.add(new ModuloPoly(true, BigInteger.ONE, indexZeta));
        fstLayer.add(new ModuloPoly(false, BigInteger.ONE, indexZeta));
        moduloPolyTree.add(fstLayer);

        while (powerX > 1) {

            powerX = powerX / 2;
            indexZeta = indexZeta.multiply(BigInteger.TWO);

            List<ModuloPoly> lstLayer = moduloPolyTree.getLast();
            List<ModuloPoly> newLayer = new ArrayList<>(2 * lstLayer.size());
            for (ModuloPoly poly : lstLayer) {
                BigInteger powerZeta = poly.getPowerZeta();
                if (poly.getPlus()) {
                    powerZeta = powerZeta.add(poly.getIndexZeta().divide(BigInteger.TWO));
                }
                ModuloPoly plusPoly = new ModuloPoly(true, powerZeta, indexZeta);
                ModuloPoly minusPoly = new ModuloPoly(false, powerZeta, indexZeta);
                newLayer.add(plusPoly);
                newLayer.add(minusPoly);
            }
            moduloPolyTree.add(newLayer);
        }
    }

    /**
     * Computes the arrays {@code zetas} and {@code zetasInverted} by exponentiating the given 2N-th root of unity.
     *
     * <p>Traversal is done layer by layer from roots to leaves through {@code moduloPolyTree}.
     * For each {@code ModuloPoly} object in a layer, this method generates the constant
     * (ζ_indexZeta)^powerZeta, where ζ_{2N} is the provided 2N-th root of unity modulo q.
     * The corresponding inverse of this constant is also generated.</p>
     *
     * @param rootOfUnity the 2N-th root of unity modulo q (ζ_{2N})
     */
    private void generateArrays(BigInteger rootOfUnity) {
        BigInteger nRoot = BigInteger.TWO.multiply(BigInteger.valueOf(n));
        for (List<ModuloPoly> layer: moduloPolyTree) {
            for (int i = 0; i < layer.size(); i = i + 2) {  // There is still + zeta, - zeta. So save it just as one zeta (the plus one).
                ModuloPoly poly = layer.get(i);
                BigInteger power = poly.getPowerZeta();
                BigInteger index = poly.getIndexZeta();
                BigInteger z = rootOfUnity.modPow(nRoot.divide(index), q).modPow(power, q);
                BigInteger zInverted = z.modPow(BigInteger.valueOf(-1), q);
                zetas.add(z);
                zetasInverted.add(zInverted);
            }
        }
    }

    /**
     * Finds the prime factors of the given number.
     *
     * <p>Based on the approach described
     * <a href="https://stackoverflow.com/questions/15347174/python-finding-prime-factors">here</a>.</p>
     *
     * @param x the number to be factored
     * @return a set of {@code BigInteger} values representing the prime factors of {@code x}
     */

    private Set<BigInteger> findPrimeFactors(BigInteger x) {
        BigInteger i = BigInteger.TWO;
        Set<BigInteger> primeFactors = new HashSet<>();
        while ((i.multiply(i)).compareTo(x) <= 0) {  // Stop in square root.
            if (x.mod(i).equals(BigInteger.ZERO)) {  // i divides x.
                x = x.divide(i);
                primeFactors.add(i);
            } else {
                i = i.add(BigInteger.ONE);  // Increment i.
            }
        }
        if (x.compareTo(BigInteger.ONE) > 0) {  // There remains one prime.
            primeFactors.add(x);
        }
        return primeFactors;
    }

    /**
     * Computes a 2n-th primitive root of unity modulo q.
     *
     * <p>Algorithm outline:</p>
     * <ol>
     *   <li>Find a generator g of the group ℤ<sub>q</sub>.</li>
     *   <li>The primitive root is then g^((q − 1) / (2n)).</li>
     * </ol>
     * <p>To find g:</p>
     * <ol>
     *     <li>Randomly incrementally choose candidate values for g.</li>
     *     <li>Check that g^(q − 1) ≡ 1 (mod q).</li>
     *     <li>Factorize (q − 1) and ensure there is no smaller exponent y such that g^y ≡ 1 (mod q).</li>
     * </ol>
     */
    private BigInteger computePrimitiveRoot() {
        BigInteger exp = (q.subtract(BigInteger.ONE)).divide((BigInteger.TWO).multiply(BigInteger.valueOf(n)));
        Set<BigInteger> primeFactors = findPrimeFactors(q.subtract(BigInteger.ONE));
        for (BigInteger g = BigInteger.TWO; g.compareTo(q) < 0; g = g.add(BigInteger.ONE)) {
            BigInteger x = g.modPow(exp, q);
            if ((g.modPow(q.subtract(BigInteger.ONE), q)).compareTo(BigInteger.ONE) != 0) {
                continue;
            }
            boolean isPrimitive = true;
            for (BigInteger pf : primeFactors) {
                if (g.modPow((q.subtract(BigInteger.ONE)).divide(pf), q).compareTo(BigInteger.ONE) == 0) {
                    isPrimitive = false;
                    break;
                }
            }
            if (isPrimitive) {
                return x;
            }
        }
        return BigInteger.valueOf(-1);
    }

    private void computeZetaArrays() {
        computeNttTree();
        BigInteger primitiveRoot = computePrimitiveRoot();
        generateArrays(primitiveRoot);
    }

    public PolynomialConfig(int n, BigInteger q) {
        this.n = n;
        this.q = q;
        this.zetas = new ArrayList<>(n - 1);
        this.zetasInverted = new ArrayList<>(n - 1);
        this.moduloPolyTree = new ArrayList<>((int) (Math.log(n) / Math.log(2)));  // that is log_2(n)
        computeZetaArrays();
    }

    int getN() {
        return n;
    }

    BigInteger getQ() {
        return q;
    }

    List<BigInteger> getZetas() {
        return List.copyOf(zetas);
    }

    List<BigInteger> getZetasInverted() {
        return List.copyOf(zetasInverted);
    }

    /**
     * Check that this and imputed polynomial are compatible.
     *
     * <p>Check that both polynomials are of the same degree (n-1) and their coefficients are modulo same constant (q).</p>
     * @param b configuration of a polynomial that will be checked against this polynomial's configuration
     */
    void assertCompatibleWith(PolynomialConfig b) {
        if (this.n != b.n) {
            throw new IllegalArgumentException("Polynomials must have the same degree n");
        }
        if (!this.q.equals(b.q)) {
            throw new IllegalArgumentException("Polynomials must use the same modulus q");
        }
    }
}
