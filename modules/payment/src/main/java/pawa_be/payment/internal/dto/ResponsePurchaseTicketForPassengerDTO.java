package pawa_be.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ResponsePurchaseTicketForPassengerDTO {
    private BigDecimal remainingBalance;
}
