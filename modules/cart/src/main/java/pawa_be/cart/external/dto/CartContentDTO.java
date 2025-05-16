package pawa_be.cart.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CartContentDTO {
    private final String name;
    private final BigDecimal amountInVND;
    private final long quantity;
    private final TicketType ticketType;
    private String lineID;
    private String lineName;
    private String startStation;
    private String endStation;
}
