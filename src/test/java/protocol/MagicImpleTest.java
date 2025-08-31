package protocol;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MagicImpleTest {

    private static final BigInteger Q = BigInteger.valueOf(17);
    /**
     * Names for all tests for hint function are of a form hintFunction<b><expected result><L or R>,
     * where in case of: ... the expected result is 0, L tests the smallest number still from the interval
     *                                             and R tests the highest number still from the interval.
     *                   ... the expected result is 1, L tests number by one smaller than in case the result is 0,
     *                                             and R tests number by one bigger than in case the result is 0.
     */

    @Test
    void hintFunction00L() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(0, magic.hintFunction(BigInteger.valueOf(13), 0));
    }

    @Test
    void hintFunction01L() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(1, magic.hintFunction(BigInteger.valueOf(12), 0));
    }

    @Test
    void hintFunction10L() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(0, magic.hintFunction(BigInteger.valueOf(14), 1));

    }

    @Test
    void hintFunction11L() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(1, magic.hintFunction(BigInteger.valueOf(13), 1));
    }

    @Test
    void hintFunction00R() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(0, magic.hintFunction(BigInteger.valueOf(4), 0));
    }

    @Test
    void hintFunction01R() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(1, magic.hintFunction(BigInteger.valueOf(5), 0));
    }

    @Test
    void hintFunction10R() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(0, magic.hintFunction(BigInteger.valueOf(5), 1));
    }

    @Test
    void hintFunction11R() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(1, magic.hintFunction(BigInteger.valueOf(6), 1));
    }

    @Test
    void signalFunction() {
        MagicImple magic = new MagicImple(Q);
        EngineImple engine = new EngineImple(new Random(123));
        assertEquals(0, magic.signalFunction(engine, BigInteger.valueOf(14)));  // .getRandomBit() = 1
        assertEquals(0, magic.signalFunction(engine, BigInteger.valueOf(13)));  // .getRandomBit() = 0
        assertEquals(1, magic.signalFunction(engine, BigInteger.valueOf(13)));  // .getRandomBit() = 1
        assertEquals(1, magic.signalFunction(engine, BigInteger.valueOf(12)));  // .getRandomBit() = 0
        assertEquals(0, magic.signalFunction(engine, BigInteger.valueOf(4)));  // .getRandomBit() = 0
        assertEquals(0, magic.signalFunction(engine, BigInteger.valueOf(5)));  // .getRandomBit() = 1
        assertEquals(1, magic.signalFunction(engine, BigInteger.valueOf(6)));  // .getRandomBit() = 1
        assertEquals(1, magic.signalFunction(engine, BigInteger.valueOf(5)));  // .getRandomBit() = 0
    }

    /**
     * Names for all tests for symmetric modulo are of a form symmetricModulo<position><converted>,
     * where: ... position is either Z as zero or H as half.
     *        ... converted is either T as true or F as false.
     * This covers all four numbers (2 close to the 0 and 2 close to the half of the q) where
     * half of them is not yet converted by applying symmetric modulo
     * and the other half is already converted by applying symmetric modulo.
     */

    @Test
    void symmetricModuloZF() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(BigInteger.valueOf(0), magic.symmetricModulo(BigInteger.valueOf(0)));
    }

    @Test
    void symmetricModuloZT() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(BigInteger.valueOf(-1), magic.symmetricModulo(BigInteger.valueOf(16)));
    }

    @Test
    void symmetricModuloHF() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(BigInteger.valueOf(8), magic.symmetricModulo(BigInteger.valueOf(8)));
    }

    @Test
    void symmetricModuloHT() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(BigInteger.valueOf(-8), magic.symmetricModulo(BigInteger.valueOf(9)));
    }

    // Trying just tuples that can occur in the input with high probability.
    @Test
    void robustExtractor() {
        MagicImple magic = new MagicImple(Q);
        assertEquals(0, magic.robustExtractor(BigInteger.valueOf(4), 0));
        assertEquals(1, magic.robustExtractor(BigInteger.valueOf(8), 1));
    }
}