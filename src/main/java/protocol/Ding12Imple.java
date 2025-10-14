package protocol;

import protocol.random.RandomCustom;

import java.math.BigInteger;

/**
 * The {@code Ding12Imple} class represents all functions needed to secretly transform information to other party
 * without intruder knowing.
 * <p>Implements functions Hint function, Signal function and robust Extractor. All from the article
 * <a href="https://eprint.iacr.org/2012/688.pdf">Ding</a><\p>
 * <p>Also implements Symmetric modulo - modulo that results values from interval [-(q-1)/2, (q-1)/2],
 * where q is odd modulus.</p>
 *
 * @author Martin Zimka
 */
public class Ding12Imple {

    private final BigInteger q;

    public Ding12Imple(BigInteger q) {
        this.q = q;
    }

    /**
     * Returns hint whether input lies in interval [-floor(q/4) + b, +floor(q/4) + b] or does not.
     *
     * @param x input value which position is being examined
     * @param b flag telling which interval to take into consideration
     * @return 0 if input value lies in the interval, 1 if it does not
     */
    public int hintFunction(BigInteger x, int b) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        BigInteger leftBound = q.divide(BigInteger.valueOf(4)).negate().add(BigInteger.valueOf(b));  // floor after the division is implicit here
        BigInteger rightBound = q.divide(BigInteger.valueOf(4)).add(BigInteger.valueOf(b));  // floor after the division is implicit here
        return (x.compareTo(leftBound) >= 0 && x.compareTo(rightBound) <= 0) ? 0 : 1;
    }

    /**
     * Randomly generates one bit value and call {@link #hintFunction(BigInteger, int)} with it and input {@code y}.
     *
     * @param rc object of a class {@code RandomCustom} needed for generating random bit
     * @param y input value which position will be examined in function {@code hintFunction}
     * @return result of {@code hintFunction}
     */
    public int signalFunction(RandomCustom rc, BigInteger y) {
        int b = rc.getRandomInt(2);
        return hintFunction(y, b);
    }

    /**
     * Applies so-called Symmetric modulo to value in Z_q. Modulus q is an odd value.
     *
     * @param r BigInteger input value in Z_q
     * @return r transformed to interval [-(q-1) / 2; +(q-1) / 2]
     */
    public BigInteger symmetricModulo(BigInteger r) {
        r = r.mod(q);  // Make sure that r is in Z_q.
        return r.compareTo((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO)) <= 0 ? r : r.subtract(q);
    }

    /**
     * Move input value {@code x} to the proximity of 0 and return its parity.
     *
     * @param x BigInteger input value
     * @param w signal whether to move {@code x} by (q-1) / 2 or not
     * @return parity of input moved to the proximity of 0 (in interval [-(q-1) / 2; +(q-1) / 2])
     */
    public int robustExtractor(BigInteger x, int w) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        BigInteger multiplied = BigInteger.valueOf(w).multiply((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO));
        BigInteger added = x.add(multiplied);
        return symmetricModulo(added).mod(BigInteger.TWO).intValue();
    }
}
