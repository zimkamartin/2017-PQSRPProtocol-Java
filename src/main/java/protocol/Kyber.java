package protocol;

public final class Kyber {

    private Kyber() {
        throw new UnsupportedOperationException("Utility class");
    }

    // SOURCE: https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java#L353
    // Applied changes because of much bigger Q. Source of the changes:
    // https://chatgpt.com/share/6835c5c5-e99c-800e-9bd4-1d25f3cddba7
    private static int rejectionSampling(Engine engine, Polynomial outputBuffer, int coeffOff, int len, byte[] inpBuf, int inpBufLen)
    {
        int ctr = 0;
        int pos = 0;

        while (ctr < len && pos + 8 <= inpBufLen)
        {
            // Extract val0 (4 bytes → int)
            int val0 = ((inpBuf[pos] & 0xFF)) |
                    ((inpBuf[pos + 1] & 0xFF) << 8) |
                    ((inpBuf[pos + 2] & 0xFF) << 16) |
                    ((inpBuf[pos + 3] & 0xFF) << 24);
            val0 = val0 & 0x7FFFFFFF; // Optional: constrain to 31 bits
            pos += 4;

            if (val0 < engine.KyberQ)
            {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val0);
                ctr++;
            }

            if (ctr >= len)
                break;

            // Extract val1 (next 4 bytes)
            int val1 = ((inpBuf[pos] & 0xFF)) |
                    ((inpBuf[pos + 1] & 0xFF) << 8) |
                    ((inpBuf[pos + 2] & 0xFF) << 16) |
                    ((inpBuf[pos + 3] & 0xFF) << 24);
            val1 = val1 & 0x7FFFFFFF;
            pos += 4;

            if (val1 < engine.KyberQ)
            {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val1);
                ctr++;
            }
        }

        return ctr;
    }

    // SOURCE: https://github.com/bcgit/bc-java/blob/2f4d33d57797dcc3fe9bd4ecb07ee0557ff58185/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java#L315
    // Almost identical with that. Changes:
    // - using class Symmetric located in this project,
    // - removed transposed (always False),
    // - removed for loop j
    static void generateUniformPolynomial(Engine engine, Polynomial a, byte[] seed)
    {
        Symmetric symmetric = engine.getSymmetric();
        int KyberGenerateMatrixNBlocks = (  // tbh have no idea what is going on here
                (
                        12 * Engine.KyberN
                                / 8 * (1 << 12)
                                / Engine.KyberQ + symmetric.xofBlockBytes
                )
                        / symmetric.xofBlockBytes
        );
        int kyberK = engine.getKyberK();
        int i, k, ctr, off;
        //byte[] buf = new byte[KyberGenerateMatrixNBlocks * symmetric.xofBlockBytes + 2];  // [smth * 168 + 2]
        byte[] buf = new byte[symmetric.xofBlockBytes * 2 + 3]; // Add 3 in case of leftover "off"
        for (i = 0; i < kyberK; i++)
        {
            symmetric.xofAbsorb(seed, (byte) i);
            symmetric.xofSqueezeBlocks(buf, 0, symmetric.xofBlockBytes * KyberGenerateMatrixNBlocks);

            int buflen = KyberGenerateMatrixNBlocks * symmetric.xofBlockBytes;
            ctr = rejectionSampling(engine, a, i * 256, 256, buf, buflen);

            while (ctr < 256) {
                //off = buflen % 3;
                off = buflen % 4;
                for (k = 0; k < off; k++) {
                    buf[k] = buf[buflen - off + k];
                }
                symmetric.xofSqueezeBlocks(buf, off, symmetric.xofBlockBytes * 2);
                buflen = off + symmetric.xofBlockBytes;
                // Error in code Section Unsure
                ctr += rejectionSampling(engine, a, i * 256 + ctr, 256 - ctr, buf, buflen);
            }
        }
    }

    // This is just for now since I am lost with generateUniformPolynomial and will look at it later :)
    static void generateUniformPolynomialV02(Engine engine, Polynomial a, byte[] seed) {
        for (int i = 0; i < engine.KyberN; i++) {
            byte[] rByte = new byte[1];
            engine.getRandomBytes(rByte);
            int unsignedByte = rByte[0] & 0xFF;
            int value = unsignedByte % engine.KyberQ;
            a.setCoeffIndex(i, value);
        }
    }
}
