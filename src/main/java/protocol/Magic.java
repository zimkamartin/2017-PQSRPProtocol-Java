package protocol;

import java.math.BigInteger;

/**
 * Represents all functions needed to secretly transform information to other party without intruder knowing.
 * <p>
 * Implements functions Hint function, Signal function and robust Extractor. All from the article
 * https://eprint.iacr.org/2017/1196.pdf
 * Also implements Symmetric modulo as defined in
 * https://youtu.be/h5pfTIE6slU?si=-EeOGTV0QD5QzbpY&t=543
 * although only for odd q, since q will be always odd (prime).
 * </p>
 */
public interface Magic {
    int hintFunction(BigInteger x, int b);
    int signalFunction(Engine e, BigInteger y);
    BigInteger symmetricModulo(BigInteger r);
    int robustExtractor(BigInteger x, int w);
}
