package protocol;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * The {@code ByteArrayWrapperTest} class tests the following methods in the class {@code ByteArrayWrapper}:
 * <ul>
 *     <li>constructor {@code ByteArrayWrapper(List<Integer>)}</li>
 *     <li>{@code concatWith(ByteArrayWrapper}</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class ByteArrayWrapperTest {

    // Number of rounds in tests.
    private static final int NUMBEROFROUNDS = 111;

    /**
     * Tests {@code NUMBEROFROUNDS}-times constructor {@code ByteArrayWrapper(List<Integer>)}.
     * <p>
     * Tests that conversion from list of Integers to byte[] object work.
     * </p>
     */
    @Test
    public void convertIntegerListToBAW() {

        List<Integer> input = Arrays.asList(-123, 120, 0, -7, 1);
        byte[] expected = new byte[]{-123, 120, 0, -7, 1};

        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            ByteArrayWrapper result = new ByteArrayWrapper(input);
            assertArrayEquals(expected, result.getData());
        }
    }

    /**
     * Tests {@code NUMBEROFROUNDS}-times constructor {@code concatWith(ByteArrayWrapper)}.
     * <p>
     * Tests concatenation of two byte[] objects.
     * </p>
     */
    @Test
    public void concatWith() {

        ByteArrayWrapper a = new ByteArrayWrapper(new byte[]{123, -120, 0, -7, 1});
        ByteArrayWrapper b = new ByteArrayWrapper(new byte[]{-111, 5, -1, 2, 127});
        ByteArrayWrapper expected = new ByteArrayWrapper(new byte[]{123, -120, 0, -7, 1, -111, 5, -1, 2, 127});

        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            ByteArrayWrapper result = a.concatWith(b);
            assertEquals(expected, result);
        }
    }
}