package pawa_be.profile.external.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class RequestRegisterPassengerDTO {
    @NotBlank
    @Pattern(regexp = "^[\\p{L}\\s]{1,50}$", message = "First name must contain only letters and spaces")
    private String passengerFirstName;

    @NotBlank
    @Pattern(regexp = "^[\\p{L}\\s]{1,50}$", message = "Middle name must contain only letters and spaces")
    private String passengerMiddleName;

    @NotBlank
    @Pattern(regexp = "^[\\p{L}\\s]{1,50}$", message = "Last name must contain only letters and spaces")
    private String passengerLastName;

    @NotBlank
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must start with 0 and contain 10 digits")
    private String passengerPhone;

    @NotBlank
    @Pattern(regexp = "^[\\w\\s,./-]+$", message = "Address contains invalid characters")
    private String passengerAddress;

    @NotNull
    @Past(message = "Date of birth must be in the past")
    private LocalDate passengerDateOfBirth;

    @Pattern(regexp = "^\\d{12}$", message = "National ID must be exactly 12 digits")
    private String nationalID;

    @Pattern(regexp = "^[a-zA-Z0-9]{0,15}$", message = "Student ID must be alphanumeric and max 15 characters")
    private String studentID;

    private Boolean hasDisability;
    private Boolean isRevolutionary;
}
