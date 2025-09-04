package protocol.random;

public interface RandomCustom {

    /**
     * @param bytes - byte array which will be filed by random bytes
     */
    void nextBytes(byte[] bytes);

    /**
     * @param bound - upper bound of the interval
     * @return random int value between 0 (inclusive) and the specified value bound (exclusive)
     */
    int nextInt(int bound);
}
