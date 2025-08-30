package protocol;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.HALF_UP;

class MlkemImple implements Mlkem{

    private final int n;
    private final BigInteger q;
    private final int unifNeededNumOfBits;  // the number of needed bits for generating 1 coefficient by Uniform distribution
    private final int unifNeededNumOfBytes;
    private final BigInteger unifMask;  // used when sampling coefficient for Uniform distribution

    MlkemImple(int n, BigInteger q) {
        this.n = n;
        this.q = q;
        this.unifNeededNumOfBits = this.q.subtract(BigInteger.ONE).bitLength();
        this.unifNeededNumOfBytes = (this.unifNeededNumOfBits + 7) / 8;
        this.unifMask = BigInteger.ONE.shiftLeft(this.unifNeededNumOfBits).subtract(BigInteger.ONE);
    }

    private int rejectionSampling(List<BigInteger> outputBuffer, int coeffOff, int len, byte[] inpBuf, int inpBufLen) {
        int ctr, pos;  // number of sampled coeffs and possition in inpBuf
        BigInteger val;  // candidate for coefficient
        ctr = pos = 0;
        while (ctr < len && pos + unifNeededNumOfBytes <= inpBufLen) {  // while I need coefficient and while I have enough bytes for next coefficient
            byte[] bufBytes = Arrays.copyOfRange(inpBuf, pos, pos + unifNeededNumOfBytes);
            BigInteger bufBI = new BigInteger(1, bufBytes);
            val = bufBI.and(unifMask);
            pos += unifNeededNumOfBytes;
            if (val.compareTo(q) < 0) {
                outputBuffer.set(coeffOff + ctr, val);
                ctr++;
            }
        }
        return ctr;
    }

    /**
     * Compute the lowest amount of XOF block bytes needed for sampling.
     * <p>
     * Should be: ((number of bytes needed for 1 coefficient) * (Probability of correct coefficient sampled) ^ -1 + xBB) / xBB
     * where xBB is xofBlockBytes.
     * </p>
     */
    private int getKyberGenerateMatrixNBlocks(Engine e) {
        BigDecimal numerator = BigDecimal.valueOf(n)
                .multiply(BigDecimal.valueOf(unifNeededNumOfBytes))
                .multiply(new BigDecimal(unifMask));
        BigDecimal denominator = new BigDecimal(q.subtract(BigInteger.ONE));
        BigDecimal fraction = numerator.divide(denominator, 11, HALF_UP);  // choose precision, currently set to 11 decimal places
        BigDecimal result = fraction
                .add(BigDecimal.valueOf(e.getXofBlockBytes()))
                .divide(BigDecimal.valueOf(e.getXofBlockBytes()), 11, HALF_UP);
        return result.setScale(0, CEILING).intValueExact();  // = the closest needed higher amount of xBB to generate for sampling
    }
    
    @Override
    public void generateUniformPolynomialNtt(Engine e, List<BigInteger> out, byte[] seed) {
        int KyberGenerateMatrixNBlocks = getKyberGenerateMatrixNBlocks(e);

        int k, ctr, off;
        int buflen = KyberGenerateMatrixNBlocks * e.getXofBlockBytes();
        byte[] buf = new byte[buflen];
        e.xofAbsorb(seed);
        e.xofSqueezeBlocks(buf, 0, buflen);

        ctr = rejectionSampling(out, 0, n, buf, buflen);  // number of sampled coefficients

        while (ctr < n) {  // we did not sample enough coeffs
            off = buflen % unifNeededNumOfBytes;  // how many unused bytes is in buf?
            for (k = 0; k < off; k++) {  // move unused bytes to the beginning of the buf
                buf[k] = buf[buflen - off + k];
            }
            e.xofSqueezeBlocks(buf, off, buflen - off);  // fill the rest of buf
            ctr += rejectionSampling(out, ctr, n - ctr, buf, buflen);
        }
    }

    private static long convertByteTo24BitUnsignedInt(byte[] x, int offset) {
        long r = (long)(x[offset] & 0xFF);
        r = r | (long)((long)(x[offset + 1] & 0xFF) << 8);
        r = r | (long)((long)(x[offset + 2] & 0xFF) << 16);
        return r;
    }

    // TODO: Change it for dynamic eta.
    // TODO: Figure out suitable eta value for given n and q.
    @Override
    public void generateCbdPolynomial(List<BigInteger> out, byte[] bytes, int eta) {
        long t, d;
        int a, b;
        // urobit to be trikov, potom s
        // aj s random etou
        for (int i = 0; i < n / 4; i++) {  // When eta is equal to 3.
            t = convertByteTo24BitUnsignedInt(bytes, 3 * i);
            d = t & 0x00249249;
            d = d + ((t >> 1) & 0x00249249);
            d = d + ((t >> 2) & 0x00249249);
            for (int j = 0; j < 4; j++)
            {
                a = (short)((d >> (6 * j + 0)) & 0x7);
                b = (short)((d >> (6 * j + 3)) & 0x7);
                short diffShort = (short)(a - b);
                BigInteger diffBI = BigInteger.valueOf(diffShort);
                out.set(4 * i + j, diffBI.mod(q));
            }
        }
    }
}
