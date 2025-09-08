package protocol.server;

import protocol.*;
import protocol.polynomial.ClassicalPolynomial;
import protocol.polynomial.NttPolynomial;
import protocol.polynomial.PolynomialConfig;
import protocol.random.RandomCustom;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static protocol.polynomial.Utils.*;

public class ServerImple implements Server {

    private final int n;
    private final ProtocolConfiguration protocolConfiguration;
    private final RandomCustom randomCustomImple;
    private final Ding12Imple ding12;
    private final PolynomialConfig polynomialConfig;

    public ServerImple(RandomCustom random, int n, BigInteger q, int eta) {
        this.n = n;
        this.randomCustomImple = random;
        this.protocolConfiguration = new ProtocolConfiguration(this.n, q, eta);
        this.polynomialConfig = new PolynomialConfig(this.n, q);
        this.ding12 = new Ding12Imple(q);
    }

    @Override
    public ProtocolConfiguration getProtocolConfiguration() {
        return protocolConfiguration;
    }

    @Override
    public void enrollClient(ByteArrayWrapper publicSeedForA, ByteArrayWrapper I, ByteArrayWrapper salt, NttPolynomial vNtt) {
        ServersDatabase.saveClient(I.defensiveCopy(), new ClientRecord(publicSeedForA.defensiveCopy(), salt.defensiveCopy(), vNtt.defensiveCopy()));
    }

    @Override
    public ServersResponseScs computeSharedSecret(ByteArrayWrapper I, NttPolynomial piNtt) {
        I = I.defensiveCopy();
        NttPolynomial constantTwoPolyNtt = NttPolynomial.constantTwoNtt(n, polynomialConfig);
        // Extract database. //
        if (!ServersDatabase.contains(I)) {
            return null;
        }
        ByteArrayWrapper publicSeedForA = ServersDatabase.getClient(I).getPublicSeedForA();
        NttPolynomial vNtt = ServersDatabase.getClient(I).getVerifierNtt();
        ByteArrayWrapper salt = ServersDatabase.getClient(I).getSalt();
        // pj = as1' + 2e1' + v //
        // Create polynomial a from public seed.
        NttPolynomial aNtt = generateUniformPolyNtt(polynomialConfig, randomCustomImple, publicSeedForA);
        // Compute s1'.
        NttPolynomial s1PrimeNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Compute e1'.
        NttPolynomial e1PrimeNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Do all the math.
        NttPolynomial summedFstTwoTuples = multiply2NttTuplesAddThemTogetherNtt(aNtt, s1PrimeNtt, constantTwoPolyNtt, e1PrimeNtt);
        NttPolynomial pjNtt = summedFstTwoTuples.add(vNtt);
        // u = XOF(H(pi || pj)) //
        NttPolynomial uNtt = computeUNtt(polynomialConfig, randomCustomImple, piNtt.defensiveCopy(), pjNtt.defensiveCopy());
        // kj = (v + pi)s1' + uv + 2e1''' //
        // Compute e1'''.
        NttPolynomial e1TriplePrimeNtt = generateRandomErrorPolyNtt(polynomialConfig, randomCustomImple);
        // Do all the math.
        NttPolynomial bracket = vNtt.add(piNtt);
        ClassicalPolynomial kj = multiply3NttTuplesAndAddThemTogether(polynomialConfig, bracket, s1PrimeNtt, uNtt, vNtt, constantTwoPolyNtt, e1TriplePrimeNtt);
        // wj = Cha(kj) //
        List<Integer> wj = IntStream.range(0, n).mapToObj(i -> ding12.signalFunction(randomCustomImple, kj.getCoeffs().get(i))).toList();
        // sigmaj = Mod_2(kj, wj) //
        List<Integer> sigmaj = IntStream.range(0, n).mapToObj(i -> ding12.robustExtractor(kj.getCoeffs().get(i), wj.get(i))).toList();
        // skj = SHA3-256(sigmaj) //
        //System.out.println(sigmaj);
        ByteArrayWrapper skj = new ByteArrayWrapper(sigmaj).hashWrapped();
        return new ServersResponseScs(salt.defensiveCopy(), pjNtt.defensiveCopy(), List.copyOf(wj), new SessionConfigurationServer(piNtt.defensiveCopy(), pjNtt.defensiveCopy(), skj));
    }

    @Override
    public ByteArrayWrapper verifyEntities(SessionConfigurationServer sessionConfiguration, ByteArrayWrapper m1) {
        NttPolynomial piNtt = sessionConfiguration.getClientsEphPubKey();
        NttPolynomial pjNtt = sessionConfiguration.getServersEphPubKey();
        ByteArrayWrapper skj = sessionConfiguration.getSharedSecret();
        // M1' = SHA3-256(pi || pj || skj) //
        ByteArrayWrapper m1Prime = piNtt.concatWith(pjNtt).toByteArrayWrapper().concatWith(skj).hashWrapped();
        // VERIFY that M1 == M1'. If true, return M2', else return empty byte array.
        // M2' = SHA3-256(pi || M1' || skj) //
        ByteArrayWrapper m2Prime = piNtt.toByteArrayWrapper().concatWith(m1Prime).concatWith(skj).hashWrapped();
        return m1.equals(m1Prime) ? m2Prime : null;
    }
}
