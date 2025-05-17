package pawa_be.cart.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private UUID cartItemId;
    private UUID lineId;
    private UUID startStationId;
    private UUID endStationId;
    private TicketType ticketType;
    private String ticketTypeName;
    private BigDecimal price;
    private int amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String displayName;
    private String duration;
}
