package pawa_be.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class CartContentDTO {
    private final String name;
    private final BigDecimal amountInVND;
    private final long quantity;
}
