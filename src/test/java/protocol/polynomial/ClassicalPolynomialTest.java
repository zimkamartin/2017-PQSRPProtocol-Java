package protocol.polynomial;

import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ClassicalPolynomialTest {

    private static final int NUMBEROFROUNDS = 111;

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

    // Be sure that N < Q.
    private List<BigInteger> generateIncrementingList() {
        List<BigInteger> result = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            result.add(BigInteger.valueOf(i));
        }
        return result;
    }

    @Test
    public void ConvertFromNtt() {
        for (int i = 0; i < NUMBEROFROUNDS; i++) {
            List<BigInteger> nttCoefficients = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(16), BigInteger.valueOf(6), BigInteger.valueOf(12));
            NttPolynomial nttPolynomial = NttPolynomial.fromNttCoefficients(nttCoefficients, new PolynomialConfig(N, Q));
            ClassicalPolynomial classicalPolynomial = new ClassicalPolynomial(nttPolynomial, new PolynomialConfig(N, Q));
            assertEquals(generateIncrementingList(), classicalPolynomial.getCoefficients());
        }
    }
}