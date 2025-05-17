package pawa_be.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private UUID invoiceItemID;
    private String ticketType;
    private BigDecimal price;
    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;
    private String lineID;
    private String lineName;
    private String startStation;
    private String endStation;
    private int duration;
}
