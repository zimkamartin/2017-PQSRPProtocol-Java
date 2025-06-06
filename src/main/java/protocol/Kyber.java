package protocol;

public final class Kyber {

    private Kyber() {
        throw new UnsupportedOperationException("Utility class");
    }

    // SOURCE: https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java#L353
    // Applied changes because of much bigger Q.
    private static int rejectionSampling(Engine engine, Polynomial outputBuffer, int coeffOff, int len, byte[] inpBuf, int inpBufLen) {
        int ctr, pos;
        int val0, val1, val2, val3;
        ctr = pos = 0;
        while (ctr < len && pos + 15 <= inpBufLen) {
            // The following should look like this:
            // val0 = 00C[0]1.1/4 C[01]1.1/2 C[01]2.1/2 C[02]1.1/2 C[02]2.1/2 C[03]1.1/2 C[03]2.1/2 C[13]1.1/2
            // val1 = 00C[0]2.1/4 C[04]1.1/2 C[04]2.1/2 C[05]1.1/2 C[05]2.1/2 C[06]1.1/2 C[06]2.1/2 C[13]2.1/2
            // val2 = 00C[0]3.1/4 C[07]1.1/2 C[07]2.1/2 C[08]1.1/2 C[08]2.1/2 C[09]1.1/2 C[09]2.1/2 C[13]1.1/2
            // val3 = 00C[0]4.1/4 C[10]1.1/2 C[10]2.1/2 C[11]1.1/2 C[11]2.1/2 C[12]1.1/2 C[12]2.1/2 C[13]2.1/2,
            // where each block is precisely 4 bits
            // and 00C[0]1.1/4 stands for 2 zero bits followed by first fourth from byte on position 0 in inpBuf.
            val0 = ((((int)(inpBuf[pos +  0] & 0xFF)) << 22) & 0x30000000) |
                   ((((int)(inpBuf[pos +  1] & 0xFF)) << 20) & 0x0FF00000) |
                   ((((int)(inpBuf[pos +  2] & 0xFF)) << 12) & 0x000FF000) |
                   ((((int)(inpBuf[pos +  3] & 0xFF)) <<  4) & 0x00000FF0) |
                   ((((int)(inpBuf[pos + 13] & 0xFF)) >>  4) & 0x0000000F);
            val1 = ((((int)(inpBuf[pos +  0] & 0xFF)) << 24) & 0x30000000) |
                   ((((int)(inpBuf[pos +  4] & 0xFF)) << 20) & 0x0FF00000) |
                   ((((int)(inpBuf[pos +  5] & 0xFF)) << 12) & 0x000FF000) |
                   ((((int)(inpBuf[pos +  6] & 0xFF)) <<  4) & 0x00000FF0) |
                   ((((int)(inpBuf[pos + 13] & 0xFF)) >>  0) & 0x0000000F);
            val2 = ((((int)(inpBuf[pos +  0] & 0xFF)) << 26) & 0x30000000) |
                   ((((int)(inpBuf[pos +  7] & 0xFF)) << 20) & 0x0FF00000) |
                   ((((int)(inpBuf[pos +  8] & 0xFF)) << 12) & 0x000FF000) |
                   ((((int)(inpBuf[pos +  9] & 0xFF)) <<  4) & 0x00000FF0) |
                   ((((int)(inpBuf[pos + 14] & 0xFF)) >>  4) & 0x0000000F);
            val3 = ((((int)(inpBuf[pos +  0] & 0xFF)) << 28) & 0x30000000) |
                   ((((int)(inpBuf[pos + 10] & 0xFF)) << 20) & 0x0FF00000) |
                   ((((int)(inpBuf[pos + 11] & 0xFF)) << 12) & 0x000FF000) |
                   ((((int)(inpBuf[pos + 12] & 0xFF)) <<  4) & 0x00000FF0) |
                   ((((int)(inpBuf[pos + 14] & 0xFF)) >>  0) & 0x0000000F);
            pos = pos + 15;
            if (val0 < engine.KyberQ) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val0);
                ctr++;
            }
            if (ctr < len && val1 < engine.KyberQ) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val1);
                ctr++;
            }
            if (ctr < len && val2 < engine.KyberQ) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val2);
                ctr++;
            }
            if (ctr < len && val3 < engine.KyberQ) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val3);
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
