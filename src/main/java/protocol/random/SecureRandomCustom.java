package protocol.random;

import java.security.SecureRandom;

public class SecureRandomCustom implements RandomCustom {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void nextBytes(byte[] bytes) {
        secureRandom.nextBytes(bytes);
    }

    @Override
    public int nextInt(int bound) {
        return secureRandom.nextInt(bound);
    }
}
