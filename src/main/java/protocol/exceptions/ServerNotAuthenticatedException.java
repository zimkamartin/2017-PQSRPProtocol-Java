package protocol.exceptions;

public class ServerNotAuthenticatedException extends Exception {
    public ServerNotAuthenticatedException(String message) {
        super(message);
    }
}
