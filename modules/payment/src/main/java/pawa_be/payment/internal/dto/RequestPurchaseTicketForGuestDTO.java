package pawa_be.payment.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPurchaseTicketForGuestDTO {
    @NotBlank
    @Email(message = "Must be a valid email")
    private String email;

    @NotEmpty(message = "Tickets list must not be empty")
    @Valid
    private List<RequestPurchaseTicketForPassengerTicketDTO> tickets;

    @NotBlank
    String successUrl;

    @NotBlank
    String cancelUrl;
}