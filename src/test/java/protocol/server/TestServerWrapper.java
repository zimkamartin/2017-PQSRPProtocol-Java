package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.ProtocolConfiguration;
import protocol.ServersResponseScs;
import protocol.polynomial.NttPolynomial;


import java.util.Objects;

public class TestServerWrapper implements Server {

    private final Server delegate;
    private ByteArrayWrapper capturedSkj;

    public TestServerWrapper(Server delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public ProtocolConfiguration getProtocolConfiguration() {
        return delegate.getProtocolConfiguration();
    }

    @Override
    public void enrollClient(ByteArrayWrapper publicSeedForA, ByteArrayWrapper I, ByteArrayWrapper salt, NttPolynomial vNtt) {
        delegate.enrollClient(publicSeedForA, I, salt, vNtt);
    }

    @Override
    public ServersResponseScs computeSharedSecret(ByteArrayWrapper I, NttPolynomial piNtt) {
        return delegate.computeSharedSecret(I, piNtt);
    }

    @Override
    public ByteArrayWrapper verifyEntities(SessionConfigurationServer scs, ByteArrayWrapper m1) {
        capturedSkj = scs.getSharedSecret();
        return delegate.verifyEntities(scs, m1);
    }

    public ByteArrayWrapper getCapturedSkj() {
        return capturedSkj;
    }
}
