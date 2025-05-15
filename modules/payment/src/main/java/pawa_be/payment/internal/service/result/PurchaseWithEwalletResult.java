package pawa_be.payment.internal.service.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.payment.internal.dto.ResponsePurchaseTicketForPassengerDTO;

@Getter
@AllArgsConstructor
public class PurchaseWithEwalletResult {
    final PurchaseWithEWalletResultType status;
    final ResponsePurchaseTicketForPassengerDTO remainingBalance;
}
