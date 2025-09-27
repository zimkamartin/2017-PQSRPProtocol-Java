package protocol.polynomial;

import java.math.BigInteger;

/**
 * The {@code ModuloPoly} class represents an auxiliary polynomial used in conversions between
 * the polynomials in the standard form and in the NTT (Number Theoretic Transform) domain.
 *
 * <p>Each polynomial is of the form:</p>
 * <pre>
 *   X^powerX ± (ζ_indexZeta)^powerZeta
 * </pre>
 * where {@code ζ} 2N-th root of unity.
 *
 * <p>The components are represented as follows:</p>
 * <ul>
 *   <li>{@code powerX}    – the exponent of {@code X}; this value is not stored</li>
 *   <li>{@code plus}      – {@code boolean}, {@code true} if the polynomial is of the form {@code (X^... + ...)},
 *                                            {@code false} if of the form {@code (X^... - ...)}</li>
 *   <li>{@code powerZeta} – {@code BigInteger}, the exponent of {@code ζ}</li>
 *   <li>{@code indexZeta} – {@code BigInteger}, the index of 2N-th root of unity {@code ζ}</li>
 * </ul>
 *
 * <p>For background on these polynomials and their role in NTT, see
 * <a href="https://electricdusk.com/ntt.html">this blog post</a>.</p>
 *
 * @author Martin Zimka
 */
class ModuloPoly {

    private final boolean plus;
    private final BigInteger powerZeta;
    private final BigInteger indexZeta;

    ModuloPoly(boolean plus, BigInteger powerZeta, BigInteger indexZeta) {
        this.plus = plus;
        this.powerZeta = powerZeta;
        this.indexZeta = indexZeta;
    }

    boolean getPlus() {
        return plus;
    }

    BigInteger getPowerZeta() {
        return powerZeta;
    }

    BigInteger getIndexZeta() {
        return indexZeta;
    }
}