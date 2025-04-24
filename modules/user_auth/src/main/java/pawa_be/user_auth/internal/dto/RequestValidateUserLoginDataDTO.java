package pawa_be.user_auth.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestValidateUserLoginDataDTO {
    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[\\w.]+@[\\w.]+\\.(com|vn)$", message = "Email must end with .com or .vn")
    @NotBlank
    private final String email;

    @NotBlank
    @Size(min = 8, message = "Password size must be from 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%!,.]).+$",
            message = "Password must contain uppercase, lowercase, digit, and special character ('@', '#', '$', '%', '!', ',', '.')"
    )
    private String password;
}
