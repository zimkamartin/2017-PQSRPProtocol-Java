package protocol.random;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.HALF_UP;

/**
 * XOF, secureRandom and prf are inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/Symmetric.java
 * Building blocks of functions generateUniformCoefficients and generateCbdCoefficients are heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/CBD.java
 */
public class RandomCustomImple implements RandomCustom {

    private final int n;
    private final BigInteger q;
    private final int eta;

    private static final SHAKEDigest xof = new SHAKEDigest(128);  // XOF defined in protocol && XOF used in BC for public a (uniform distribution) from seed ~> speed > security
    private static final int XOFBLOCKBYTES = xof.getByteLength();  // shall be 168

    private static final SecureRandom secureRandom = new SecureRandom();  // when truly random data are needed
    private final SHAKEDigest prf = new SHAKEDigest(256);  // PRF for sampling error poly from seed ~> security > speed

    // variables used in uniform distribution:
    private final int unifNeededNumOfBytes;
    private final BigInteger unifMask;

    public RandomCustomImple(int n, BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        int unifNeededNumOfBits = this.q.subtract(BigInteger.ONE).bitLength();  // the number of needed bits for generating 1 coefficient by Uniform distribution
        this.unifNeededNumOfBytes = (unifNeededNumOfBits + 7) / 8;
        this.unifMask = BigInteger.ONE.shiftLeft(unifNeededNumOfBits).subtract(BigInteger.ONE);
    }

    @Override
    public byte[] getRandomBytes(int n) {
        byte[] byteArray = new byte[n];
        secureRandom.nextBytes(byteArray);
        return byteArray.clone();
    }

    @Override
    public int getRandomBit(int bound) {
        return secureRandom.nextInt(bound);
    }

    private int rejectionSampling(List<BigInteger> outputBuffer, int coeffOff, int len, byte[] inpBuf, int inpBufLen) {
        int ctr, pos;  // number of sampled coeffs and position in inpBuf
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
    public List<BigInteger> generateUniformCoefficients(int n, byte[] seed) {
        List<BigInteger> out = new ArrayList<>(Collections.nCopies(n, null));  // zvycajne iba cez add, netreba vytvorit a potom assignovat

        int KyberGenerateMatrixNBlocks = computeKyberGenerateMatrixNBlocks();

        int k, ctr, off;
        int buflen = KyberGenerateMatrixNBlocks * XOFBLOCKBYTES;
        byte[] buf = new byte[buflen];
        xof.reset();
        xof.update(seed, 0, seed.length);
        xof.doOutput(buf, 0, buflen);

        ctr = rejectionSampling(out, 0, n, buf, buflen);  // number of sampled coefficients

        while (ctr < n) {  // we did not sample enough coeffs
            off = buflen % unifNeededNumOfBytes;  // how many unused bytes is in buf?
            for (k = 0; k < off; k++) {  // move unused bytes to the beginning of the buf
                buf[k] = buf[buflen - off + k];
            }
            xof.doOutput(buf, off, buflen - off);  // fill the rest of buf
            ctr += rejectionSampling(out, ctr, n - ctr, buf, buflen);
        }

        return List.copyOf(out);
    }

    private int bitCountOfMUnusedBits(byte[] bytes, int byteIndex, int bitIndex, int m) {
        int inp = bytes[byteIndex] & 0xFF;  // get the unsigned value of a current byte
        int shifted = inp << bitIndex;  // get rid of already used bits
        int mask = (1 << m) - 1;  // create mask for m bits
        return Integer.bitCount(shifted & mask);  // add to a number of bits 1 in masked result
    }

    @Override
    public List<BigInteger> generateCbdCoefficients(int n, byte[] seed) {
        List<BigInteger> out = new ArrayList<>(Collections.nCopies(n, null));
        byte[] buf = new byte[(int) Math.ceil((n * 2.0 * eta) / 8.0)];
        prf.update(seed, 0, seed.length);
        prf.doFinal(buf, 0, buf.length);

        int bitIndex = 0;
        int byteIndex = 0;
        for (int i = 0; i < n; i++) {
            int a = 0;
            int b = 0;
            int count = 0;  // how many bits do I already have?
            while (count < eta) {
                int m = Math.min(eta - count, 8 - bitIndex);  // how many bits will we take from the byte
                a += bitCountOfMUnusedBits(buf, byteIndex, bitIndex, m);  // add to a number of bits 1 in masked result
                count += m;  // update number of bits that we already have
                byteIndex += (bitIndex + m == 8) ? 1 : 0;  // change byte and bit index accordingly
                bitIndex = (bitIndex + m) % 8;
            }
            count = 0;
            while (count < eta) {
                int m = Math.min(eta - count, 8 - bitIndex);  // how many bits will we take from the byte
                b += bitCountOfMUnusedBits(buf, byteIndex, bitIndex, m);  // add to a number of bits 1 in masked result
                count += m;  // update number of bits that we already have
                byteIndex += (bitIndex + m == 8) ? 1 : 0;  // change byte and bit index accordingly
                bitIndex = (bitIndex + m) % 8;
            }
            out.set(i, BigInteger.valueOf(a - b));
        }

        return List.copyOf(out);
    }
}
