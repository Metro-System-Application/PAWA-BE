package pawa_be.user_auth.internal.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pawa_be.infrastructure.common.validation.constant.RegexConstants;
import pawa_be.infrastructure.common.validation.custom.CustomDateDeserializer;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class RequestGoogleProfileDataDTO {
    @NotBlank
    @Pattern(regexp = "^[" + RegexConstants.VIETNAMESE_LETTERS + "]{1,50}$", message = "First name must contain only letters")
    private String passengerFirstName;

    @NotBlank
    @Pattern(regexp = "^[" + RegexConstants.VIETNAMESE_LETTERS + "\\s]{1,50}$", message = "Middle name must contain only letters and spaces")
    private String passengerMiddleName;

    @NotBlank
    @Pattern(regexp = "^[" + RegexConstants.VIETNAMESE_LETTERS + "]{1,50}$", message = "Last name must contain only letters")
    private String passengerLastName;

    @NotBlank
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must start with 0 and contain 10 digits")
    private String passengerPhone;

    @NotBlank
    @Pattern(regexp = "^[" + RegexConstants.VIETNAMESE_LETTERS + "0-9\\s,./-]+$", message = "Address contains invalid characters, must contain only letters, numbers, spaces and (,), (-), (.) or (/)")
    private String passengerAddress;

    @JsonDeserialize(using = CustomDateDeserializer.class)
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
