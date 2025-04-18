package pawa_be.user_auth.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;

@Getter
@AllArgsConstructor
public class RequestRegisterUserDTO {
    @Email(message = "Email must be a valid format")
    @NotBlank(message = "Email cannot be empty")
    private final String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private final String password;

    private UserRoleConfig role;
}
