package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import protocol.random.RandomCustom;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for byte[] so that we can compare them and use them as keys in database.
 * SOURCE: ChatGPT.
 */
public class ByteArrayWrapper {

    private final byte[] data;

    public ByteArrayWrapper(byte[] data) {
        this.data = data.clone();
    }

    public ByteArrayWrapper(RandomCustom rc, int numOfBytes) {
        this.data = rc.getRandomBytes(numOfBytes);
    }

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

    public byte[] hash() {
        byte[] hash = new byte[32];
        SHA3Digest sha3Digest256 = new SHA3Digest(256);
        sha3Digest256.update(data, 0, data.length);
        sha3Digest256.doFinal(hash, 0);
        return hash;
    }

    public ByteArrayWrapper hashWrapped() {
        return new ByteArrayWrapper(this.hash());
    }

    /**
     * @param b - ByteArrayWrapper object which data will be concatenated to this ByteArrayWrapper object
     * @return new ByteArrayWrapper which data will be concatenation this || b
     */
    public ByteArrayWrapper concatWith(ByteArrayWrapper b) {
        byte[] result = new byte[this.data.length + b.getData().length];
        System.arraycopy(this.data, 0, result, 0, this.data.length);
        System.arraycopy(b.getData(), 0, result, this.data.length, b.getData().length);
        return new ByteArrayWrapper(result);
    }

    public ByteArrayWrapper defensiveCopy() {  // zmazat, kedze nikde nie je mutable getter
        return new ByteArrayWrapper(this.data.clone());
    }
}
