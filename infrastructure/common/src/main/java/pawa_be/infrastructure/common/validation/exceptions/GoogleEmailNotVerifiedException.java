package pawa_be.infrastructure.common.validation.exceptions;

public class GoogleEmailNotVerifiedException extends RuntimeException {
    public GoogleEmailNotVerifiedException(String message) {
        super(message);
    }
}
