package pawa_be.infrastructure.common.validation.exceptions;

public class GoogleAccountAlreadyLinkedException extends RuntimeException {
    public GoogleAccountAlreadyLinkedException(String message) {
        super(message);
    }
}
