package protocol.polynomial;

import protocol.EngineImple;
import protocol.MlkemImple;
import protocol.ProtocolConfiguration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utils class for polynomials. All math related single-polynomial operations are in the class Polynomial,
 * remaining operations (NON-math single-polynomial or more than single-polynomial) are here.
 */
public final class Utils {

    private Utils() {}

    private static void checkAllNtt(List<Polynomial> polynomials) {
        for (Polynomial poly : polynomials) {
            if (!poly.isNtt()) {
                throw new IllegalArgumentException("All polynomials must be in NTT form");
            }
        }
    }
    /**
     * @return ab + cd, where each polynomial is in NTT form
     */
    public static Polynomial multiply2NttTuplesAddThemTogetherNtt(Polynomial aNtt, Polynomial bNtt, Polynomial cNtt, Polynomial dNtt) {
        checkAllNtt(Arrays.asList(aNtt, bNtt, cNtt, dNtt));

        Polynomial abNtt = aNtt.multiplyNtt(bNtt);
        Polynomial cdNtt = cNtt.multiplyNtt(dNtt);
        return abNtt.add(cdNtt);
    }

    /**
     * @return ab + cd + ef, where each polynomial is in NTT form
     */
    public static Polynomial multiply3NttTuplesAndAddThemTogetherNtt(Polynomial aNtt, Polynomial bNtt, Polynomial cNtt, Polynomial dNtt, Polynomial eNtt, Polynomial fNtt) {
        checkAllNtt(Arrays.asList(aNtt, bNtt, cNtt, dNtt, eNtt, fNtt));

        Polynomial addedFstTwoNtt = multiply2NttTuplesAddThemTogetherNtt(aNtt, bNtt, cNtt, dNtt);
        Polynomial efNtt = eNtt.multiplyNtt(fNtt);
        return addedFstTwoNtt.add(efNtt);
    }

    public static void getEtaNoise(ProtocolConfiguration pp, MlkemImple mlkem, EngineImple engine, List<BigInteger> r, byte[] seed) {
        byte[] buf = new byte[(int) Math.ceil((pp.getN() * 2.0 * pp.getEta()) / 8.0)];
        engine.prf(buf, seed);
        mlkem.generateCbdCoeffs(r, buf, pp.getEta());
    }

    public static Polynomial generateRandomErrorPoly(ProtocolConfiguration pc, MlkemImple mlkem, EngineImple engine) {
        List<BigInteger> eCoeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        byte[] eRandomSeed = new byte[34];
        engine.getRandomBytes(eRandomSeed);
        getEtaNoise(pc, mlkem, engine, eCoeffs, eRandomSeed);
        return new Polynomial(List.copyOf(eCoeffs), pc.getQ(), false);
    }

    public static Polynomial generateUniformPolyNtt(ProtocolConfiguration pc, MlkemImple mlkem, EngineImple engine, byte[] seed) {
        List<BigInteger> coeffs = new ArrayList<>(Collections.nCopies(pc.getN(), null));
        mlkem.generateUniformCoeffsNtt(engine, coeffs, seed.clone());
        return new Polynomial(coeffs, pc.getQ(), true);
    }

    /**
     * u = XOF(H(pi || pj))
     */
    public static Polynomial computeUNtt(ProtocolConfiguration pc, EngineImple engine, MlkemImple mlkem, Polynomial piNtt, Polynomial pjNtt) {
        byte[] seed = new byte[32];
        engine.hash(seed, piNtt.concatWith(pjNtt).toByteArray());
        return generateUniformPolyNtt(pc, mlkem, engine, seed);
    }
}
