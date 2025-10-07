package protocol;

import org.junit.Test;
import protocol.random.RandomCustom;
import protocol.random.TestPreSeededRandom;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * The {@code Ding12ImpleTest} class tests the following methods of {@link Ding12Imple}:
 * <ul>
 *     <li>{@code hintFunction(BigInteger, int)}</li>
 *     <li>{@code signalFunction(RandomCustom, BigInteger)}</li>
 *     <li>{@code symmetricModulo(BigInteger)}</li>
 *     <li>{@code robustExtractor(BigInteger, int)}</li>
 * </ul>
 *
 * <p>
 * Test method names for {@code hintFunction} follow the pattern: {@code hintFunction<b><expectedResult><L or R>},
 * where:
 * </p>
 * <ul>
 *     <li>If the expected result is {@code 0}:
 *         <ul>
 *             <li>{@code L} tests lower bound of the interval</li>
 *             <li>{@code R} tests upper bound of the interval</li>
 *         </ul>
 *     </li>
 *     <li>If the expected result is {@code 1}:
 *         <ul>
 *             <li>{@code L} tests the value one smaller than the lower bound of the interval/li>
 *             <li>{@code R} tests the value one greater than the upper bound of the interval</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * Test method names for {@code symmetricModulo} follow the pattern: {@code symmetricModulo<position><convert>},
 * where:
 * </p>
 * <ul>
 *     <li>{@code position} is either {@code Z} as zero or {@code H} as half</li>
 *     <li>{@code convert} is either {@code T} as true or {@code F} as false</li>
 * </ul>
 * <p>
 * This covers all four numbers (2 close to the 0 and 2 close to the half of the q) where half of them will not
 * be converted by applying symmetric modulo and the other half will be converted by applying symmetric modulo.
 * </p>
 * <p>Each test is provided {@code NUMBEROFROUNDS}-times.</p>
 *
 * @author Martin Zimka
 */
public class Ding12ImpleTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;

    private static final BigInteger Q = BigInteger.valueOf(17);

    @Test
    public void hintFunction00L() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(0, ding12Imple.hintFunction(BigInteger.valueOf(13), 0));
        }
    }
    
    @Test
    public void hintFunction01L() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(1, ding12Imple.hintFunction(BigInteger.valueOf(12), 0));
        }
    }
    
    @Test
    public void hintFunction10L() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(0, ding12Imple.hintFunction(BigInteger.valueOf(14), 1));
        }
    }

    @Test
    public void hintFunction11L() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(1, ding12Imple.hintFunction(BigInteger.valueOf(13), 1));
        }
    }
    
    @Test
    public void hintFunction00R() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(0, ding12Imple.hintFunction(BigInteger.valueOf(4), 0));
        }
    }

    @Test
    public void hintFunction01R() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(1, ding12Imple.hintFunction(BigInteger.valueOf(5), 0));
        }
    }

    @Test
    public void hintFunction10R() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(0, ding12Imple.hintFunction(BigInteger.valueOf(5), 1));
        }
    }
    
    @Test
    public void hintFunction11R() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(1, ding12Imple.hintFunction(BigInteger.valueOf(6), 1));
        }
    }
    
    @Test
    public void signalFunction() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            RandomCustom rc = new TestPreSeededRandom(5, Q, 3, 123);  // do not care about n and eta
            assertEquals(0, ding12Imple.signalFunction(rc, BigInteger.valueOf(14)));  // .getRandomBit() = 1
            assertEquals(0, ding12Imple.signalFunction(rc, BigInteger.valueOf(13)));  // .getRandomBit() = 0
            assertEquals(1, ding12Imple.signalFunction(rc, BigInteger.valueOf(13)));  // .getRandomBit() = 1
            assertEquals(1, ding12Imple.signalFunction(rc, BigInteger.valueOf(12)));  // .getRandomBit() = 0
            assertEquals(0, ding12Imple.signalFunction(rc, BigInteger.valueOf(4)));  // .getRandomBit() = 0
            assertEquals(0, ding12Imple.signalFunction(rc, BigInteger.valueOf(5)));  // .getRandomBit() = 1
            assertEquals(1, ding12Imple.signalFunction(rc, BigInteger.valueOf(6)));  // .getRandomBit() = 1
            assertEquals(1, ding12Imple.signalFunction(rc, BigInteger.valueOf(5)));  // .getRandomBit() = 0
        }
    }
    
    @Test
    public void symmetricModuloZF() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(BigInteger.valueOf(0), ding12Imple.symmetricModulo(BigInteger.valueOf(0)));
        }
    }
    
    @Test
    public void symmetricModuloZT() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(BigInteger.valueOf(-1), ding12Imple.symmetricModulo(BigInteger.valueOf(16)));
        }
    }
    
    @Test
    public void symmetricModuloHF() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(BigInteger.valueOf(8), ding12Imple.symmetricModulo(BigInteger.valueOf(8)));
        }
    }

    @Test
    public void symmetricModuloHT() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(BigInteger.valueOf(-8), ding12Imple.symmetricModulo(BigInteger.valueOf(9)));
        }
    }

    // Trying just tuples that can occur in the input with high probability.
    @Test
    public void robustExtractor() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            Ding12Imple ding12Imple = new Ding12Imple(Q);
            assertEquals(0, ding12Imple.robustExtractor(BigInteger.valueOf(4), 0));
            assertEquals(1, ding12Imple.robustExtractor(BigInteger.valueOf(8), 1));
        }
    }
}