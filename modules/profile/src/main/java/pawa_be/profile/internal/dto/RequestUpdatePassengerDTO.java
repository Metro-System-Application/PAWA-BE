package pawa_be.profile.internal.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestUpdatePassengerDTO {
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must start with 0 and contain 10 digits")
    private String passengerPhone;

    @Pattern(regexp = "^[\\w\\s,./-]+$", message = "Address contains invalid characters")
    private String passengerAddress;
}

