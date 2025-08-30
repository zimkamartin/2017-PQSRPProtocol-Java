package protocol;

import java.math.BigInteger;

public class MagicImple implements Magic {

    private final BigInteger q;

    MagicImple(BigInteger q) {
        this.q = q;
    }

    @Override
    public int hintFunction(BigInteger x, int b) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        BigInteger leftBound = q.divide(BigInteger.valueOf(4)).negate().add(BigInteger.valueOf(b));  // floor after the division is implicit here
        BigInteger rightBound = q.divide(BigInteger.valueOf(4)).add(BigInteger.valueOf(b));  // floor after the division is implicit here
        return (x.compareTo(leftBound) >= 0 && x.compareTo(rightBound) <= 0) ? 0 : 1;
    }

    @Override
    public int signalFunction(Engine e, BigInteger y) {
        int b = e.getRandomBit();
        return hintFunction(y, b);
    }

    @Override
    public BigInteger symmetricModulo(BigInteger r) {
        r = r.mod(q);  // Make sure that r is in Z_q.
        return r.compareTo((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO)) <= 0 ? r : r.subtract(q);
    }

    @Override
    public int robustExtractor(BigInteger x, int w) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        BigInteger multiplied = BigInteger.valueOf(w).multiply((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO));
        BigInteger added = x.add(multiplied);
        return symmetricModulo(added).mod(BigInteger.TWO).intValue();
    }
}
