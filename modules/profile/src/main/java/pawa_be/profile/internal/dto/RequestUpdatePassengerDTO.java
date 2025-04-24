package pawa_be.profile.internal.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.infrastructure.common.validation.constant.RegexConstants;

@Getter
@Setter
@AllArgsConstructor
public class RequestUpdatePassengerDTO {
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must start with 0 and contain 10 digits")
    private String passengerPhone;

    @NotBlank
    @Pattern(regexp = "^[" + RegexConstants.VIETNAMESE_LETTERS + "0-9\\s,./-]+$", message = "Address contains invalid characters, must contain only letters, numbers, spaces and (,), (-), (.) or (/)")
    private String passengerAddress;
}

