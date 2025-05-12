package pawa_be.cart.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResponseEditCartItemDTO {
    private UUID cartItemId;
    private int newAmount;
    private BigDecimal newPrice;
}
