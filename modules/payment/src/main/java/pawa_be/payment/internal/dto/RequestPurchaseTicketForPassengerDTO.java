package pawa_be.payment.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPurchaseTicketForPassengerDTO {
    @NotEmpty(message = "Tickets list must not be empty")
    @Valid
    private List<RequestPurchaseTicketForPassengerTicketDTO> tickets;
}