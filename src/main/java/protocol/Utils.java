package protocol;

import java.util.List;

public final class Utils {

    private Utils(){}

    /**
     * Enough for our case, so when inp contains only ones and zeros.
     */
    public static byte[] convertIntegerListToByteArrayAndHashIt(int n, EngineImple engine, List<Integer> inp) {
        byte[] hash = new byte[32];
        byte[] inpByteArray = new byte[n];
        for (int i = 0; i < n; i++) {
            inpByteArray[i] = inp.get(i).byteValue();
        }
        engine.hash(hash, inpByteArray);
        return hash;
    }

    public static byte[] concatenateTwoByteArraysAndHashThem(EngineImple engine, byte[] a, byte[] b) {
        byte[] input = new byte[a.length + b.length];
        System.arraycopy(a, 0, input, 0, a.length);
        System.arraycopy(b, 0, input, a.length, b.length);
        byte[] hashed = new byte[32];
        engine.hash(hashed, input);
        return hashed;
    }

    public static byte[] concatenateThreeByteArraysAndHash(EngineImple engine, byte[] a, byte[] b, byte[] c) {
        byte[] input = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, input, 0, a.length);
        System.arraycopy(b, 0, input, a.length, b.length);
        System.arraycopy(c, 0, input, a.length + b.length, c.length);
        byte[] hashed = new byte[32];
        engine.hash(hashed, input);
        return hashed;
    }
}
