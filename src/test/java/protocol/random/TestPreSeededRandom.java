package protocol.random;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.HALF_UP;

/**
 * The {@code TestPreSeededRandom} class is identical with the class {@link RandomCustomImple}
 * (implementing interface {@link RandomCustom}), only instead of {@code SecureRandom},
 * preseeded (argument in constructor) {@code Random} is used.
 *
 * @author Martin Zimka
 */
public class TestPreSeededRandom implements RandomCustom {

    private final int n;
    private final BigInteger q;
    private final int eta;  // CBD values will be sampled from interval [-eta; +eta]

    // XOF defined in the protocol && XOF used in BC for public a (uniform distribution) from seed
    private static final SHAKEDigest xof = new SHAKEDigest(128);
    private static final int XOFBLOCKBYTES = xof.getByteLength();  // shall be 168

    private final Random random = new Random();
    // PRF for sampling error poly from seed
    private final SHAKEDigest prf = new SHAKEDigest(256);

    // variables used in uniform distribution:
    private final int unifNeededNumOfBytes;
    private final BigInteger unifMask;

    public TestPreSeededRandom(int n, BigInteger q, int eta, long seed) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        this.random.setSeed(seed);
        // the number of needed bits for generating 1 coefficient by Uniform distribution
        int unifNeededNumOfBits = this.q.subtract(BigInteger.ONE).bitLength();
        this.unifNeededNumOfBytes = (unifNeededNumOfBits + 7) / 8;
        this.unifMask = BigInteger.ONE.shiftLeft(unifNeededNumOfBits).subtract(BigInteger.ONE);
    }

    @Override
    public byte[] getRandomBytes(int n) {
        byte[] byteArray = new byte[n];
        random.nextBytes(byteArray);
        return byteArray;
    }

    @Override
    public int getRandomInt(int bound) {
        return random.nextInt(bound);
    }

    private int rejectionSampling(List<BigInteger> outputBuffer, int len, byte[] inpBuf, int inpBufLen) {
        int ctr, pos;  // number of sampled coeffs and position in inpBuf
        BigInteger val;  // candidate for coefficient
        ctr = pos = 0;
        while (ctr < len && pos + unifNeededNumOfBytes <= inpBufLen) {
            byte[] bufBytes = Arrays.copyOfRange(inpBuf, pos, pos + unifNeededNumOfBytes);
            BigInteger bufBI = new BigInteger(1, bufBytes);
            val = bufBI.and(unifMask);
            pos += unifNeededNumOfBytes;
            if (val.compareTo(q) < 0) {
                outputBuffer.add(val);
                ctr++;
            }
        }
        return ctr;
    }

    private int computeKyberGenerateMatrixNBlocks() {
        BigDecimal numerator = BigDecimal.valueOf(n)
                .multiply(BigDecimal.valueOf(unifNeededNumOfBytes))
                .multiply(new BigDecimal(unifMask));
        BigDecimal denominator = new BigDecimal(q.subtract(BigInteger.ONE));
        BigDecimal fraction = numerator.divide(denominator, 11, HALF_UP);  // choose precision, currently set to 11 decimal places
        BigDecimal result = fraction
                .add(BigDecimal.valueOf(XOFBLOCKBYTES))
                .divide(BigDecimal.valueOf(XOFBLOCKBYTES), 11, HALF_UP);
        return result.setScale(0, CEILING).intValueExact();  // = the closest needed higher amount of xBB to generate for sampling
    }

    @Override
    public List<BigInteger> generateUniformCoefficients(byte[] seed) {
        List<BigInteger> out = new ArrayList<>(n);

        int KyberGenerateMatrixNBlocks = computeKyberGenerateMatrixNBlocks();

        int k, ctr, off;
        int buflen = KyberGenerateMatrixNBlocks * XOFBLOCKBYTES;
        byte[] buf = new byte[buflen];
        xof.reset();
        xof.update(seed, 0, seed.length);
        xof.doOutput(buf, 0, buflen);

        ctr = rejectionSampling(out, n, buf, buflen);  // number of sampled coefficients

        while (ctr < n) {  // we did not sample enough coeffs
            off = buflen % unifNeededNumOfBytes;  // how many unused bytes is in buf?
            for (k = 0; k < off; k++) {  // move unused bytes to the beginning of the buf
                buf[k] = buf[buflen - off + k];
            }
            xof.doOutput(buf, off, buflen - off);  // fill the rest of buf
            ctr += rejectionSampling(out, n - ctr, buf, buflen);
        }

        return out;
    }

    private int bitCountOfMUnusedBits(byte b, int bitIndex, int m) {
        int inp = b & 0xFF;  // get the unsigned value of a byte
        int shifted = inp << bitIndex;  // get rid of already used bits
        int mask = (1 << m) - 1;  // create mask for m bits
        return Integer.bitCount(shifted & mask);  // add to a number of bits 1 in masked result
    }

    private int readEtaBits(BitCursor bc, byte[] buf, int eta) {
        int count = 0;  // how many bits do I already have?
        int ones = 0;  // number of ones

        while (count < eta) {
            int m = Math.min(eta - count, 8 - bc.getBitIndex());  // how many bits will we take from the byte
            ones += bitCountOfMUnusedBits(buf[bc.getByteIndex()], bc.getBitIndex(), m);
            count += m;  // update number of bits that we already have
            bc.updateIndices(m);
        }

        return ones;
    }

    @Override
    public List<BigInteger> generateCbdCoefficients(byte[] seed) {
        List<BigInteger> out = new ArrayList<>(n);
        byte[] buf = new byte[(int) Math.ceil((n * 2.0 * eta) / 8.0)];
        prf.update(seed, 0, seed.length);
        prf.doFinal(buf, 0, buf.length);

        BitCursor bc = new BitCursor();
        for (int i = 0; i < n; i++) {
            int a = readEtaBits(bc, buf, eta);
            int b = readEtaBits(bc, buf, eta);
            out.add(BigInteger.valueOf(a - b));
        }

        return out;
    }
}
