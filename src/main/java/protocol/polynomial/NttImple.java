package protocol.polynomial;

import java.math.BigInteger;
import java.util.*;

/**
 * Computes arrays zetas and zetas inverted for converting to and from NTT representation of a polynomial.
 * Heavily inspired by https://electricdusk.com/ntt.html
 */
public class NttImple {

    private final int n;
    private final BigInteger q;

    private final List<BigInteger> zetas;
    private final List<BigInteger> zetasInverted;

    private final List<List<ModuloPoly>> nttTree;

    /**
     * Computes tree of modulo polynomials. Everything from layer X^(n//2) to X^1.
     */
    private void computeNttTree() {
        int powerX = n / 2;
        BigInteger indexZeta = BigInteger.valueOf(4);
        List<ModuloPoly> fstLayer = new ArrayList<>(2);
        fstLayer.add(new ModuloPoly(true, BigInteger.ONE, indexZeta));
        fstLayer.add(new ModuloPoly(false, BigInteger.ONE, indexZeta));
        nttTree.add(fstLayer);

        while (powerX > 1) {

            powerX = powerX / 2;
            indexZeta = indexZeta.multiply(BigInteger.TWO);

            List<ModuloPoly> lstLayer = nttTree.getLast();
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
            nttTree.add(newLayer);
        }
    }

    /**
     * Generates arrays zetas and zetas inverted by exponentiating parameter zeta.
     * @param zeta
     */
    private void generateArrays(BigInteger zeta) {
        BigInteger nRoot = BigInteger.TWO.multiply(BigInteger.valueOf(n));
        for (List<ModuloPoly> layer: nttTree) {
            for (int i = 0; i < layer.size(); i = i + 2) {  // There is still + zeta, - zeta. So save it just as one zeta (the plus one).
                ModuloPoly poly = layer.get(i);
                BigInteger power = poly.getPowerZeta();
                BigInteger index = poly.getIndexZeta();
                BigInteger z = zeta.modPow(nRoot.divide(index), q).modPow(power, q);
                BigInteger zInverted = z.modPow(BigInteger.valueOf(-1), q);
                zetas.add(z);
                zetasInverted.add(zInverted);
            }
        }
    }

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
     * Compute 2*n-th primitive root of one modulo q.
     * <p>
     * Find generator g of a group Z_q. Primitive root is then g ^ ((q - 1) / 2 * n).
     * Firstly randomly incrementally choose possible g. Check that g ^ (q - 1) is congruent to 1 modulo q.
     * Factorize (q - 1) and check, that there is no smoller exponent y s. t. g ^ y is congruent to 1 modulo q.
     * If not, g is our generator. Compute primitive root and return it.
     * </p>
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
        BigInteger zeta = computePrimitiveRoot();
        generateArrays(zeta);
    }

    public NttImple(int n, BigInteger q) {
        this.n = n;
        this.q = q;
        this.zetas = new ArrayList<>(n - 1);
        this.zetasInverted = new ArrayList<>(n - 1);
        this.nttTree = new ArrayList<>((int) (Math.log(n) / Math.log(2)));  // that is log_2(n)
        computeZetaArrays();
    }

    /**
     * @return attribute zetasArray, so all precomputed coefficients used to convert polynomial to its NTT form.
     */
    public List<BigInteger> getZetasArray() {
        return List.copyOf(this.zetas);
    }

    /**
     * @return attribute zetasInvertedArray, so all precomputed coefficients used to convert polynomial back from its NTT form.
     */
    public List<BigInteger> getZetasInvertedArray() {
        return List.copyOf(this.zetasInverted);
    }
}
