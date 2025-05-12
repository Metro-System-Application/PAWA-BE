package pawa_be.cart.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class RequestEditCartItemDTO {
    @NotNull
    private UUID cartItemId;
    @Min(value = 0, message = "Min value to edit to must be 0")
    private int newAmount;
}
