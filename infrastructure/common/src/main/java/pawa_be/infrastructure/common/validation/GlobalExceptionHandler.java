package pawa_be.infrastructure.common.validation;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.stripe.exception.StripeException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.common.validation.exceptions.AlreadyExistsException;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponseDTO<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        GenericResponseDTO<Map<String, String>> response = new GenericResponseDTO<>(
                false,
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericResponseDTO<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            final String rawField = cv.getPropertyPath().toString();
            final String field = rawField.substring(rawField.lastIndexOf('.') + 1);
            final String message = cv.getMessage();
            errors.put(field, message);
        });

        GenericResponseDTO<Map<String, String>> response = new GenericResponseDTO<>(
                false,
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponseDTO<Map<String, String>>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();

        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause instanceof InvalidFormatException formatException) {
            String fieldName = formatException.getPath().getLast().getFieldName();
            String detailedMessage = formatException.getMessage();
            final String malformedValue = formatException.getValue().toString();
            final String formattedDetailedMessage = detailedMessage.substring(0, detailedMessage.indexOf('\n'));
            errors.put(fieldName, String.format(
                    "Invalid value '%s', %s", malformedValue, formattedDetailedMessage));
        } else {
            errors.put("request", "Malformed request body. Please check your input.");
        }

        GenericResponseDTO<Map<String, String>> response = new GenericResponseDTO<>(
                false,
                "Invalid input",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ NotFoundException.class, IllegalArgumentException.class, RuntimeException.class, AlreadyExistsException.class })
    public ResponseEntity<GenericResponseDTO<?>> handleCommonExceptions(RuntimeException ex) {
        HttpStatus status;

        if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof AlreadyExistsException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(new GenericResponseDTO<>(false, ex.getMessage(), null), status);
    }

    @ExceptionHandler({ IOException.class })
    public ResponseEntity<GenericResponseDTO<?>> handleDeadlyExceptions(IOException ex) {
        return new ResponseEntity<>(new GenericResponseDTO<>(false, ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ StripeException.class })
    public ResponseEntity<GenericResponseDTO<?>> handleStripeException(StripeException ex) {
        return new ResponseEntity<>(new GenericResponseDTO<>(false, ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}