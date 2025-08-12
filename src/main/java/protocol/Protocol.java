package protocol;

import static protocol.Kyber.generateUniformPolynomial;

class Protocol {
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT
    private static final String I = "identity123";
    private static final String PWD = "password123";
    private static final byte[] SALT = "salt123".getBytes();
    // THIS IS NOT HOW TO DO IT !!! THIS IS JUST FOR PROOF-OF-CONCEPT !!! THIS IS NOT HOW TO DO IT

    private Engine engine;
    private static Symmetric symmetric;
    private Seeds clientsSeeds;
    public byte[] publicSeed;

    private static Seeds createSeeds() {
        // seed1 = SHA3-256(salt||SHA3-256(I||pwd))
        String innerInput = I.concat(PWD);
        byte[] innerHash = new byte[32];
        symmetric.hash_h(innerHash, innerInput.getBytes(), 0);
        byte[] outerInput = new byte[SALT.length + innerHash.length];
        System.arraycopy(SALT, 0, outerInput, 0, SALT.length);
        System.arraycopy(innerHash, 0, outerInput, SALT.length, innerHash.length);
        byte[] seed1 = new byte[32];
        symmetric.hash_h(seed1, outerInput, 0);
        // seed2 = SHA3-256(seed1)
        byte[] seed2 = new byte[32];
        symmetric.hash_h(seed2, seed1, 0);

        return new Seeds(seed1, seed2);
    }

    Protocol(Engine engine) {
        this.engine = engine;
        symmetric = this.engine.getSymmetric();
        this.clientsSeeds = createSeeds();
        this.publicSeed = new byte[34];
    }

    private Polynomial createUniformPoly() {
        Polynomial result = new Polynomial(new int[engine.KyberN]);
        engine.getRandomBytes(publicSeed);
        generateUniformPolynomial(engine, result, publicSeed);
        return result;
    }

    void run() {
        // TODO Create public polynomial a.
        Polynomial a = createUniformPoly();
        // TODO Phase 0.
        // TODO Phase 1.
        // TODO Phase 2.
    };
}
