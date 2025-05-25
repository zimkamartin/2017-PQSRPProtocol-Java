package protocol;

public class Main {

    public static void main(String[] args) {
        Engine engine = new Engine();
        Protocol protocol = new Protocol(engine);
        protocol.run();
    }
}