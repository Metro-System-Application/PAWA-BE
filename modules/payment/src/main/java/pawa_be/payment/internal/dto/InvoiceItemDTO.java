package pawa_be.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pawa_be.payment.internal.enumeration.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private UUID invoiceItemId;
    private String ticketType;
    private TicketStatus status;
    private BigDecimal price;
    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;
    private String lineId;
    private String lineName;
    private String startStation;
    private String endStation;
    private int duration;
}
