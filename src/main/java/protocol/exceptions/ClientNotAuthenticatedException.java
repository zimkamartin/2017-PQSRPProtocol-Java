package protocol.exceptions;

public class ClientNotAuthenticatedException extends Exception {
    public ClientNotAuthenticatedException(String message) {
        super(message);
    }
}
