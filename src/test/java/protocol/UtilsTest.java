package protocol;

import java.math.BigInteger;

class UtilsTest {

    private static final int N = 4;
    private static final BigInteger Q = BigInteger.valueOf(17);

//    // Be sure that n < q.
//    private List<BigInteger> generateIncrementingList(int n) {
//        List<BigInteger> result = new ArrayList<>(n);
//        for (int i = 0; i < n; i++) {
//            result.add(BigInteger.valueOf(i));
//        }
//        return result;
//    }
//
//    @Test
//    void getEtaNoise() {  // TODO add when mlkem is final
//    }
//
//    @Test
//    void generateRandomErrorPolyNtt() {  // TODO add when mlkem is final
//    }
//
//    @Test
//    void computeUNtt() {  // TODO add when mlkem is final
//    }
//
//    @Test
//    void multiply2NttTuplesAndAddThemTogetherNtt() {
//        NttImple ntt = new NttImple(N, Q);
//        List<BigInteger> aNtt = generateIncrementingList(N);
//        List<BigInteger> bNtt = Arrays.asList(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
//        List<BigInteger> cNtt = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
//        List<BigInteger> dNtt = Arrays.asList(BigInteger.TEN, BigInteger.TEN, BigInteger.TEN, BigInteger.TEN);
//        List<BigInteger> output = Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, bNtt, cNtt, dNtt);
//        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6));
//        assertEquals(expected, output);
//    }
//
//    @Test
//    void multiply3NttTuplesAndAddThemTogether() {
//        NttImple ntt = new NttImple(N, Q);
//        List<BigInteger> aNtt = generateIncrementingList(N);
//        List<BigInteger> bNtt = Arrays.asList(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
//        List<BigInteger> cNtt = Arrays.asList(BigInteger.TWO, BigInteger.TWO, BigInteger.TWO, BigInteger.TWO);
//        List<BigInteger> dNtt = Arrays.asList(BigInteger.TEN, BigInteger.TEN, BigInteger.TEN, BigInteger.TEN);
//        List<BigInteger> output = Utils.multiply3NttTuplesAndAddThemTogether(ntt, aNtt, bNtt, cNtt, dNtt, aNtt, aNtt);
//        List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(8), BigInteger.valueOf(7), BigInteger.valueOf(16), BigInteger.valueOf(13));
//        assertEquals(expected, output);
//    }
}