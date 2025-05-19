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
public class ActivationResponseDTO {
    private UUID invoiceItemID;
    private String ticketType;
    private TicketStatus status;
    private BigDecimal price;
    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;
    private String lineID;
    private String lineName;
    private UUID startStationId;
    private UUID endStationId;
    private String startStationName;
    private String endStationName;
    private int duration;
    private boolean success;
    private String message;
} 