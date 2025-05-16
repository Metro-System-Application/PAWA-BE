package pawa_be.insfrastructure.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LineItemRequestDTO {
    private final String name;
    private final long amountInVND;
    private final long quantity;

    private final String ticketType;
    private final String lineId;
    private final String lineName;
    private final String startStation;
    private final String endStation;
}
