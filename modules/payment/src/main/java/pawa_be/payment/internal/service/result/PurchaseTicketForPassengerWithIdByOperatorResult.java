package pawa_be.payment.internal.service.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.payment.internal.dto.ResponsePurchaseTicketForPassengerDTO;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PurchaseTicketForPassengerWithIdByOperatorResult {
    final PurchaseTicketForPassengerWithIdByOperatorResultType status;
    final ResponsePurchaseTicketForPassengerDTO remainingBalance;
}
