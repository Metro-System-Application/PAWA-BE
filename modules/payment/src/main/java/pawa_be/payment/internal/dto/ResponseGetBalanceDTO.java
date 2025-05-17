package pawa_be.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class ResponseGetBalanceDTO {
    BigDecimal balance;
}
