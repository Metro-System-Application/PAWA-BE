package pawa_be.insfrastructure.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LineItemRequestDTO {
    private final String name;
    private final long amountInVND;
    private final long quantity;
}
