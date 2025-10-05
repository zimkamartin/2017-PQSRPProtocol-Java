package protocol.random;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitCursorTest {

    private static final int NUMBEROFROUNDS = 111;

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
