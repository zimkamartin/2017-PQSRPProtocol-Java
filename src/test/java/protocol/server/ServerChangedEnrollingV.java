package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.ProtocolConfiguration;
import protocol.ServersResponseScs;
import protocol.polynomial.NttPolynomial;
import protocol.polynomial.PolynomialConfig;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ServerChangedEnrollingV implements Server {

    private final Server delegate;

    public ServerChangedEnrollingV(Server delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public ProtocolConfiguration getProtocolConfiguration() {
        return delegate.getProtocolConfiguration();
    }

    private NttPolynomial generateRandomV() {

        int n = delegate.getProtocolConfiguration().getN();
        BigInteger q = delegate.getProtocolConfiguration().getQ();
        int qInt = q.intValue();

        Random random = new Random();

        List<BigInteger> coeffs = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            coeffs.add(BigInteger.valueOf(random.nextInt(qInt)));
        }

        return NttPolynomial.fromClassicalCoefficients(coeffs, new PolynomialConfig(n, q));
    }

    @Override
    public void enrollClient(ByteArrayWrapper publicSeedForA, ByteArrayWrapper I, ByteArrayWrapper salt, NttPolynomial vNtt) {
        delegate.enrollClient(publicSeedForA, I, salt, generateRandomV());
    }

    @Override
    public ServersResponseScs computeSharedSecret(ByteArrayWrapper I, NttPolynomial piNtt) {
        return delegate.computeSharedSecret(I, piNtt);
    }

    @Override
    public ByteArrayWrapper verifyKeysEntities(SessionConfigurationServer scs, ByteArrayWrapper m1) {
        return delegate.verifyKeysEntities(scs, m1);
    }
}
