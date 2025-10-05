package protocol.random;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.HALF_UP;

/**
 * The {@code RandomCustomImple} class implements {@code RandomCustom} interface.
 *
 * <p>Building blocks of functions generateUniformCoefficients and generateCbdCoefficients are heavily inspired by
 * <a href="https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java">mlkem/MLKEMIndCpa.java</a>
 * and
 * <a href="https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/CBD.java">mlkem/CBD.java</a>
 * </p>
 *
 * @author Martin Zimka
 */
public class RandomCustomImple implements RandomCustom {

    private final int n;
    private final BigInteger q;
    private final int eta;  // CBD values will be sampled from interval [-eta; +eta]

    // XOF defined in the protocol && XOF used in BC for public a (uniform distribution) from seed
    private static final SHAKEDigest xof = new SHAKEDigest(128);
    private static final int XOFBLOCKBYTES = xof.getByteLength();  // shall be 168

    private static final SecureRandom secureRandom = new SecureRandom();
    // PRF for sampling error poly from seed
    private final SHAKEDigest prf = new SHAKEDigest(256);

    // variables used in uniform distribution:
    private final int unifNeededNumOfBytes;
    private final BigInteger unifMask;

    public RandomCustomImple(int n, BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        // the number of needed bits for generating 1 coefficient by Uniform distribution
        int unifNeededNumOfBits = this.q.subtract(BigInteger.ONE).bitLength();
        this.unifNeededNumOfBytes = (unifNeededNumOfBits + 7) / 8;
        this.unifMask = BigInteger.ONE.shiftLeft(unifNeededNumOfBits).subtract(BigInteger.ONE);
    }

    @Override
    public byte[] getRandomBytes(int n) {
        byte[] byteArray = new byte[n];
        secureRandom.nextBytes(byteArray);
        return byteArray;
    }

    @Override
    public int getRandomInt(int bound) {
        return secureRandom.nextInt(bound);
    }

    /**
     * Samples BigInteger coefficients modulo {@code q} from a given byte array using rejection sampling.
     *
     * <p>The method is generic and works for any modulus {@code q}. It proceeds as follows
     * until the required number of coefficients is generated or the input buffer is exhausted:</p>
     * <ol>
     *   <li>Read the next {@code unifNeededNumOfBytes}, i.e. the smallest multiple of 8
     *       greater than or equal to the number of bits needed to represent {@code q-1}.</li>
     *   <li>Convert these bytes to a positive BigInteger candidate.</li>
     *   <li>Apply a bitmask ({@code unifMask}) to truncate the value to the required bit length.</li>
     *   <li>If the candidate is less than {@code q}, accept it as a valid coefficient.</li>
     * </ol>
     *
     * @param outputBuffer list where accepted coefficients are stored
     * @param len number of coefficients to generate
     * @param inpBuf input buffer providing random bytes
     * @param inpBufLen length of the input buffer
     * @return number of coefficients successfully sampled
     */
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

    /**
     * Computes the minimum number of XOF block bytes required for sampling.
     *
     * <p>The calculation is based on the expected number of bytes needed to sample
     * {@code n} coefficients modulo {@code q} via rejection sampling. The formula is:</p>
     *
     * <pre>
     *     ((unifNeededNumOfBytes) * (probOfCorrCoeffSampled) ^ -1 + xBB) / xBB
     * </pre>
     *
     * where:
     * <ul>
     *   <li>{@code unifNeededNumOfBytes} — number of bytes needed to represent a candidate coefficient</li>
     *   <li>{@code probOfCorrCoeffSampled} — probability of correct coefficient is sampled/li>
     *   <li>{@code xBB} — block size of the XOF (in bytes)</li>
     * </ul>
     *
     * <p>The result is the smallest multiple of {@code xBB} sufficient for sampling.</p>
     *
     * @return number of XOF blocks needed
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

    /**
     * Samples a list of uniformly distributed BigInteger values derived from the given seed.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Compute the required number of XOF blocks</li>
     *   <li>Generate a byte array of (that length) * {@link #XOFBLOCKBYTES} bytes from the given seed</li>
     *   <li>Sample coefficients using {@link #rejectionSampling(List, int, byte[], int)}</li>
     *   <li>If fewer than {@code n} coefficients were obtained:
     *     <ol>
     *       <li>Determine how many bytes remained unused after the last sampling</li>
     *       <li>Shift the unused bytes to the beginning of the buffer</li>
     *       <li>Generate new bytes (again from the seed) to fill the remainder of the buffer</li>
     *       <li>Sample additional coefficients via {@link #rejectionSampling(List, int, byte[], int)}</li>
     *     </ol>
     *   </li>
     * </ol>
     *
     * @param seed seed used to generate the uniform data
     * @return a list of {@code n} sampled values, suitable as polynomial coefficients (in either standard or NTT domain)
     */
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

    /**
     * Counts the number of set bits (value 1) in a given byte, starting from a specified bit position.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Convert the byte to its unsigned integer representation</li>
     *   <li>Shift left by {@code bitIndex} positions to discard already used bits</li>
     *   <li>Create a mask covering the next {@code m} bits</li>
     *   <li>Apply the mask and count the set bits using {@link Integer#bitCount(int)}</li>
     * </ol>
     *
     * @param b        the byte in which bits are counted
     * @param bitIndex the starting bit position
     * @param m        the number of bits to count from {@code bitIndex}
     * @return the number of bits set to 1 in the specified range
     */
    private int bitCountOfMUnusedBits(byte b, int bitIndex, int m) {
        int inp = b & 0xFF;  // get the unsigned value of a byte
        int shifted = inp << bitIndex;  // get rid of already used bits
        int mask = (1 << m) - 1;  // create mask for m bits
        return Integer.bitCount(shifted & mask);  // add to a number of bits 1 in masked result
    }

    /**
     * Counts the number of set bits (value 1) in {@code eta} bits from the buffer,
     * starting at the position defined by the given {@link protocol.random.BitCursor}.
     *
     * <p>Algorithm:</p>
     * <ol>
     *     <li>Determine how many bits can mostly be taken from the current byte</li>
     *     <li>Count the number of set bits in those bits using {@link #bitCountOfMUnusedBits(byte, int, int)}</li>
     *     <li>Update the total count of set bits and processed bits</li>
     *     <li>Advance the {@link protocol.random.BitCursor} accordingly</li>
     * </ol>
     *
     * @param bc   cursor tracking the current byte and bit position in {@code buf}
     * @param buf  input buffer to read from
     * @param eta  number of bits to read
     * @return number of set bits among the {@code eta} bits read
     */
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

    /**
     * Samples a list of BigInteger values form interval [-eta; +eta] using the Centered Binomial Distribution,
     * derived from the given seed.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Generate a byte array of sufficient length from the given seed</li>
     *   <li>Repeat {@code n} times to sample all coefficients:</li>
     *   <ol>
     *       <li>Count {@code a}, the number of set bits (1s) in {@code eta} bits from the buffer</li>
     *       <li>Advance the {@link protocol.random.BitCursor} accordingly</li>
     *       <li>Count {@code b}, the number of set bits (1s) in the next {@code eta} bits</li>
     *       <li>Advance the {@link protocol.random.BitCursor} accordingly</li>
     *       <li>Compute the coefficient as {@code a - b}</li>
     *   </ol>
     * </ol>
     *
     * @param seed seed for generating buffer for Centered Binomial Distribution data
     * @return a list of {@code n} sampled values, suitable as polynomial coefficients in the standard domain
     */
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
