package protocol;

import protocol.utils.Symmetric;

public class Main {

    public static void main(String[] args) {
        Engine engine = new Engine();
        Protocol protocol = new Protocol(engine);
        protocol.run();
        // Seeds seeds = createSeeds();
    }
}