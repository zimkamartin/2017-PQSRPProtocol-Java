package protocol.random;

/**
 * The {@code BitCursor} class tracks the current bit and byte position in a byte array.
 *
 * <p>It consists of the following attributes:</p>
 * <ul>
 *   <li>{@code bitIndex}  – {@code int}, initially set to 0; the index of the current bit within the current byte</li>
 *   <li>{@code byteIndex} – {@code int}, initially set to 0; the index of the current byte in the array</li>
 * </ul>
 *
 * <p>Apart from standard getters (although package-private), this class provides the method
 * {@link #updateIndices(int)}, which updates both indices according to the number of read bits.</p>
 *
 * @author Martin Zimka
 */
class BitCursor {

    private int bitIndex = 0;
    private int byteIndex = 0;

    BitCursor(){}

    int getBitIndex() {
        return bitIndex;
    }

    int getByteIndex() {
        return byteIndex;
    }

    /**
     * Updates both indices according to the number of read bits.
     *
     * @param count number of bits read
     */
    void updateIndices(int count) {
        byteIndex += (bitIndex + count == 8) ? 1 : 0;
        bitIndex = (bitIndex + count) % 8;
    }
}
