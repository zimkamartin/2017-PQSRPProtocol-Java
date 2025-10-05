package protocol.server;

import protocol.ByteArrayWrapper;
import protocol.ProtocolConfiguration;
import protocol.ServersResponseScs;
import protocol.polynomial.NttPolynomial;

import java.util.Objects;

/**
 * The {@code TestServerWrapper} class is a simple wrapper around an implementation of the {@link Server} interface,
 * designed to provide access to the computed shared secret.
 * <p>
 * This class takes an implementation of {@link Server} as a constructor argument.
 * All interface methods are delegated to the wrapped instance.
 * In addition, it provides the method {@link #getCapturedSkj()} to retrieve the shared secret key
 * captured during the {@code verifyKeysEntities(SessionConfigurationServer, ByteArrayWrapper)} call.
 * </p>
 *
 * @author Ondrej Kollarik
 */
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
    public ByteArrayWrapper verifyKeysEntities(SessionConfigurationServer scs, ByteArrayWrapper m1) {
        capturedSkj = scs.getSharedSecret();
        return delegate.verifyKeysEntities(scs, m1);
    }

    public ByteArrayWrapper getCapturedSkj() {
        return capturedSkj;
    }
}
