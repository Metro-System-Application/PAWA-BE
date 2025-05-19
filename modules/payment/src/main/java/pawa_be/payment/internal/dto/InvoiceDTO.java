package pawa_be.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private UUID invoiceId;
    private String passengerId;
    private String email;
    private BigDecimal totalPrice;
    private LocalDateTime purchasedAt;
    private List<InvoiceItemDTO> items;
    
    public boolean isPurchased() {
        return purchasedAt != null;
    }
}
