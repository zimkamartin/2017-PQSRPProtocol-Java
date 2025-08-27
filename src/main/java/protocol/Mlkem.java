package protocol;

import java.math.BigInteger;
import java.util.List;

/**
 * Extracted (and modified) all needed functions from MLKEM.
 * <p>
 * Functions are modified so they can dynamically adapt to different N, Q, ETA.
 * However, their building blocks are heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/CBD.java
 * </p>
 */
public interface Mlkem {

    /**
     * @param e - engine where XOF is implemented
     * @param out - polynomial that will be filled by Ntt representation of a polynomial whose coefficients are sampled from a uniform distribution
     * @param seed - XOF will be seeded by this. Output should be uniform and then sampled to out
     */
    void generateUniformPolynomialNtt(Engine e, List<BigInteger> out, byte[] seed);

    /**
     * @param out - polynomial that will be filled by coefficients are sampled from a central binomial distribution
     * @param bytes - bytes used in specific way to obtain from them coefficients sampled from CBD
     * @param eta - parameter of CBD. Coefficients will be from [-eta; eta] modulo Q
     */
    void generateCbdPolynomial(List<BigInteger> out, byte[] bytes, int eta);
    }
