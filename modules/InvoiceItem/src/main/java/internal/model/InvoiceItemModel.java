package internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.profile.internal.model.Passenger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceItemID;

    @ManyToOne
    @JoinColumn(name="invoice_id", referencedColumnName = "invoiceID", nullable = false)
    private InvoiceModel invoiceModel;

    private String ticketName;
    private String ticketType;
    private BigDecimal price;

    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;

    private UUID lineID;
    private String lineName;

    private String startStation;
    private String endStation;

    private int duration;

    private LocalDateTime createdAt;
    private LocalDateTime purchasedAt;
}
