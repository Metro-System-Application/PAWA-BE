package pawa_be.ticket.external.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;

@ControllerAdvice
@Slf4j
public class ExternalApiExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<GenericResponseDTO<String>> handleHttpClientError(HttpClientErrorException ex) {
        log.error("Client error when accessing external API: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new GenericResponseDTO<>(
                        false,
                        "Error accessing external service: " + ex.getStatusText(),
                        null));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<GenericResponseDTO<String>> handleHttpServerError(HttpServerErrorException ex) {
        log.error("Server error from external API: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GenericResponseDTO<>(
                        false,
                        "External service error: " + ex.getStatusText(),
                        null));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<GenericResponseDTO<String>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Cannot access external API: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GenericResponseDTO<>(
                        false,
                        "External service unavailable. Please try again later.",
                        null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponseDTO<String>> handleRuntimeException(RuntimeException ex) {
        log.error("Error accessing external API: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponseDTO<>(
                        false,
                        "Error processing request: " + ex.getMessage(),
                        null));
    }
}
