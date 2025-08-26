package protocol;

public class ClientImple {

    private final Server server;
    private final PublicParams publicParams;
    private final byte[] publicSeedForA;
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!
    private final byte[] I = "identity123".getBytes();
    private final byte[] pwd = "password123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT !!!

    public ClientImple(Server server) {
        this.server = server;
        this.publicParams = server.getPublicParams();
        this.publicSeedForA = new byte[0];  // TODO: SOLVE!
    }

    public void enrollClient() {
        // Compute verifier.
        // server.enrollClient(publicSeedForA, I, salt, vNtt);
    }

    public void computeSharedSecret() {
        // Compute pi
        // SaltEphPublicSignal sPjNttWj = server.computeSharedSecret(I, pi);
        // Compute u
        // ...
        // Compute Shared secret - save it as an attribute of this Class  - is it the best way? Where to save secret? Or return it in some way?
    }

    public void verifyEntities() {
        // Compute M1
        // byte[] m2Prime = server.verifyEntities(m1);
        // Compute M2
    }
}
