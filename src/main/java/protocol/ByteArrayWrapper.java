package protocol;

import java.util.Arrays;

/**
 * Wrapper for byte[] so that we can compare them and use them as keys in database.
 * SOURCE: ChatGPT.
 */
public class ByteArrayWrapper {

    private final byte[] data;

    public ByteArrayWrapper(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper that = (ByteArrayWrapper) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
