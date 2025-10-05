package protocol.random;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The {@code BitCursorTest} class tests the following methods in the class {@code BitCursor}:
 * <ul>
 *     <li>{@code updateIndices(int)}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class BitCursorTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;

    /**
     * Tests {@code NUMBEROFROUNDS}-times method {@code updateIndices(int)}.
     * <p>
     * The following sequence (Byte, bit) is tested: (0, 0) + 8 = (1, 0) + 2 = (1, 2) + 3 = (1, 5) + 3 = (2, 0).
     * </p>
     */
    @Test
    public void updateIndices() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            BitCursor bc = new BitCursor();
            bc.updateIndices(8);
            assertEquals(0, bc.getBitIndex());
            assertEquals(1, bc.getByteIndex());
            bc.updateIndices(2);
            assertEquals(2, bc.getBitIndex());
            assertEquals(1, bc.getByteIndex());
            bc.updateIndices(3);
            assertEquals(5, bc.getBitIndex());
            assertEquals(1, bc.getByteIndex());
            bc.updateIndices(3);
            assertEquals(0, bc.getBitIndex());
            assertEquals(2, bc.getByteIndex());
        }
    }
}
