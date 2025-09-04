package protocol.polynomial;

import protocol.polynomial.ModuloPoly;

import java.math.BigInteger;
import java.util.*;

/**
 * Represents all math operations with objects of class Polynomial. For efficiency, everything is done in NTT domain.
 * <p>
 * When an instance is constructed, arrays zetas and zetas inverted are computed.
 * Otherwise, provides just utility functions add, inverse, subtracts, multiply, create constant two polynomial.
 * NTT stuff heavily inspired by https://electricdusk.com/ntt.html
 * </p>
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
     * It is needed for test.
     */
    public List<BigInteger> getZetasArray() {
        return List.copyOf(this.zetas);
    }

    /**
     * @return attribute zetasInvertedArray, so all precomputed coefficients used to convert polynomial back from its NTT form.
     * It is needed for test.
     */
    public List<BigInteger> getZetasInvertedArray() {
        return List.copyOf(this.zetasInverted);
    }

    /**
     * @param inputPoly - polynomial in classic form
     * @return inputPoly in Ntt form
     */
    public Polynomial convertToNtt(Polynomial inputPoly) {
        if (inputPoly.isNtt()) {
            throw new IllegalArgumentException("Input polynomial is already in NTT form");
        }
        List<BigInteger> polyNtt = new ArrayList<>(inputPoly.getCoeffs());
        int zetaIndex = 0;

        int numOfLayers = (int) (Math.log(n) / Math.log(2));
        for (int layer = 0; layer < numOfLayers; layer++) {
            int numOfSubpolys = (int) Math.pow(2, layer);
            int lenOfSubpoly = n / numOfSubpolys;
            for (int subpolyCounter = 0; subpolyCounter < numOfSubpolys; subpolyCounter++) {
                int polyLstIndex = subpolyCounter * lenOfSubpoly - 1;
                for (int subpolyIndex = polyLstIndex + 1; subpolyIndex < polyLstIndex + 1 + lenOfSubpoly / 2; subpolyIndex++) {
                    int subpolyHalfIndex = subpolyIndex + lenOfSubpoly / 2;
                    BigInteger oldSubpolyCoeff = polyNtt.get(subpolyIndex);
                    BigInteger oldSubpolyHalfCoeff = polyNtt.get(subpolyHalfIndex);
                    polyNtt.set(subpolyIndex, (oldSubpolyCoeff.subtract(zetas.get(zetaIndex).multiply(oldSubpolyHalfCoeff))).mod(q));
                    polyNtt.set(subpolyHalfIndex, (oldSubpolyCoeff.add(zetas.get(zetaIndex).multiply(oldSubpolyHalfCoeff))).mod(q));
                }
                zetaIndex++;
            }
        }

        return new Polynomial(polyNtt, inputPoly.getQ(), true);
    }

    /**
     * @param inputPoly - polynomial in Ntt form
     * @return inputPoly in classic form
     */
    public Polynomial convertFromNtt(Polynomial inputPoly) {
        if (!inputPoly.isNtt()) {
            throw new IllegalArgumentException("Input polynomial is already in classic form");
        }
        List<BigInteger> poly = new ArrayList<>(inputPoly.getCoeffs());
        int zetaIndex = zetasInverted.size() - 1;

        int numOfLayers = (int) (Math.log(n) / Math.log(2));
        for (int layer = numOfLayers - 1; layer >= 0; layer--) {
            int numOfSubpolys = (int) Math.pow(2, layer);
            int lenOfSubpoly = n / numOfSubpolys;
            for (int subpolyCounter = numOfSubpolys - 1; subpolyCounter >= 0; subpolyCounter--) {
                int polyLstIndex = subpolyCounter * lenOfSubpoly + lenOfSubpoly;
                for (int subpolyHalfIndex = polyLstIndex - 1; subpolyHalfIndex > polyLstIndex - 1 - lenOfSubpoly / 2; subpolyHalfIndex--) {
                    int subpolyIndex = subpolyHalfIndex - lenOfSubpoly / 2;
                    BigInteger oldSubpolyCoeff = poly.get(subpolyIndex);
                    BigInteger oldSubpolyHalfCoeff = poly.get(subpolyHalfIndex);
                    poly.set(subpolyIndex, oldSubpolyCoeff.add(oldSubpolyHalfCoeff).mod(q));
                    poly.set(subpolyHalfIndex, zetasInverted.get(zetaIndex).negate().multiply(oldSubpolyCoeff.subtract(oldSubpolyHalfCoeff)).mod(q));
                }
                zetaIndex--;
            }
        }

        BigInteger twoDivisor = BigInteger.TWO.modPow(BigInteger.valueOf(numOfLayers).negate(), q);
        for (int i = 0; i < n; i = i + 1) {
            poly.set(i, poly.get(i).multiply(twoDivisor).mod(q));
        }
        return new Polynomial(poly, inputPoly.getQ(), false);
    }
}
