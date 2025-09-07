package protocol;

import java.util.List;

public final class Utils {

    private Utils(){}

    /**
     * Enough for our case, so when inp contains only ones and zeros.
     */
    public static byte[] convertIntegerListToByteArray(List<Integer> inp) {
        byte[] byteArray = new byte[inp.size()];
        for (int i = 0; i < inp.size(); i++) {
            byteArray[i] = inp.get(i).byteValue();
        }
        return byteArray;
    }
}
