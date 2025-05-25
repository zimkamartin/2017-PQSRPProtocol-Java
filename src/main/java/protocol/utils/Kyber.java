package protocol.utils;

public final class Kyber {

    private Kyber() {
        throw new UnsupportedOperationException("Utility class");
    }

    // SOURCE: https://github.com/bcgit/bc-java/blob/2f4d33d57797dcc3fe9bd4ecb07ee0557ff58185/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java#L315
    // Almost identical with that. Changes:
    // - TODO
    public void generateMatrix(PolyVec[] aMatrix, byte[] seed, boolean transposed)
    {
        int i, j, k, ctr, off;
        byte[] buf = new byte[KyberGenerateMatrixNBlocks * symmetric.xofBlockBytes + 2];
        for (i = 0; i < kyberK; i++)
        {
            for (j = 0; j < kyberK; j++)
            {
                if (transposed)
                {
                    symmetric.xofAbsorb(seed, (byte) i, (byte) j);
                }
                else
                {
                    symmetric.xofAbsorb(seed, (byte) j, (byte) i);
                }
                symmetric.xofSqueezeBlocks(buf, 0, symmetric.xofBlockBytes * KyberGenerateMatrixNBlocks);

                int buflen = KyberGenerateMatrixNBlocks * symmetric.xofBlockBytes;
                ctr = rejectionSampling(aMatrix[i].getVectorIndex(j), 0, MLKEMEngine.KyberN, buf, buflen);

                while (ctr < MLKEMEngine.KyberN)
                {
                    off = buflen % 3;
                    for (k = 0; k < off; k++)
                    {
                        buf[k] = buf[buflen - off + k];
                    }
                    symmetric.xofSqueezeBlocks(buf, off, symmetric.xofBlockBytes * 2);
                    buflen = off + symmetric.xofBlockBytes;
                    // Error in code Section Unsure
                    ctr += rejectionSampling(aMatrix[i].getVectorIndex(j), ctr, MLKEMEngine.KyberN - ctr, buf, buflen);
                }
            }
        }

    }
}
