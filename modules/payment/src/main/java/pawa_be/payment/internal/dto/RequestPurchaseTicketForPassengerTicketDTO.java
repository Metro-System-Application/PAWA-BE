package pawa_be.payment.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pawa_be.ticket.external.enumerator.TicketType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPurchaseTicketForPassengerTicketDTO {
    @NotNull(message = "Ticket type must not be null")
    private TicketType ticketType;

    @Min(value = 1, message = "Amount must be at least 1")
    private int amount;
}