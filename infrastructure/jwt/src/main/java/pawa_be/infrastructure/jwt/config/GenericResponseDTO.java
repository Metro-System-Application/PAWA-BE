package pawa_be.infrastructure.jwt.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class GenericResponseDTO<T> {
    @Schema(example = "true", description = "Indicates if the request was successful")
    private final boolean success;

    @Schema(example = "User registered successfully", description = "A human-readable message")
    private final String message;

    private final T data;

    public GenericResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
