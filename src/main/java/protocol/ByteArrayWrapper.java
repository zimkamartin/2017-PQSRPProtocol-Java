package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import protocol.random.RandomCustom;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code ByteArrayWrapper} class is an immutable wrapper around a {@code byte array}.
 *
 * <p>This wrapping of raw byte array provides the following advantages:</p>
 * <ul>
 *   <li>Supports proper equality checks via {@link #equals(Object)}</li>
 *   <li>Ensures immutability by cloning input and output arrays</li>
 *   <li>Provides utility methods such as hashing and concatenation</li>
 * </ul>
 *
 * @author Martin Zimka
 */
public class ByteArrayWrapper {

    private final byte[] data;

    /**
     * Perform most simple wrap around inputted data.
     *
     * @param data byte array
     */
    public ByteArrayWrapper(byte[] data) {
        this.data = data.clone();
    }

    /**
     * Generate randomly {@code numOfBytes} bytes and wrap them.
     *
     * @param rc object of class {@link RandomCustom}
     * @param numOfBytes number of bytes that should be randomly generated
     */
    public ByteArrayWrapper(RandomCustom rc, int numOfBytes) {
        this.data = rc.getRandomBytes(numOfBytes);
    }

    /**
     * Convert list of integers of values [-128; 127] to byte array and wrap it.
     *
     * @param dataList list of Integer values from range [-128; 127]
     */
    public ByteArrayWrapper(List<Integer> dataList) {
        byte[] dataBA = new byte[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            dataBA[i] = dataList.get(i).byteValue();
        }
        this.data = dataBA;
    }

    public byte[] getData() {
        return data.clone();
    }

    /**
     * SOURCE: ChatGPT
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper that = (ByteArrayWrapper) o;
        return Arrays.equals(data, that.data);
    }

    /**
     * SOURCE: ChatGPT
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Hash byte array data using Sha3-256 digest.
     *
     * @return hash as byte[] object
     */
    public byte[] hash() {
        byte[] hash = new byte[32];
        SHA3Digest sha3Digest256 = new SHA3Digest(256);
        sha3Digest256.update(data, 0, data.length);
        sha3Digest256.doFinal(hash, 0);
        return hash;
    }

    /**
     * Hash byte array data using Sha3-256 digest.
     *
     * @return hash as wrapped byte[] object
     */
    public ByteArrayWrapper hashWrapped() {
        return new ByteArrayWrapper(this.hash());
    }

    /**
     * Concatenate data of this object with data of object b.
     *
     * @param b - ByteArrayWrapper object which data will be concatenated to this ByteArrayWrapper object
     * @return new ByteArrayWrapper which data will be concatenation this || b
     */
    public ByteArrayWrapper concatWith(ByteArrayWrapper b) {
        byte[] result = new byte[this.data.length + b.getData().length];
        System.arraycopy(this.data, 0, result, 0, this.data.length);
        System.arraycopy(b.getData(), 0, result, this.data.length, b.getData().length);
        return new ByteArrayWrapper(result);
    }
}
