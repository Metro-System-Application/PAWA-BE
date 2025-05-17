package pawa_be.payment.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pawa_be.payment.internal.enumeration.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_item")
@Data
public class InvoiceItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceItemID;

    @ManyToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "invoiceID", nullable = false, updatable = false)
    private InvoiceModel invoiceModel;

    @Column(nullable = false)
    private String ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private String lineID;

    @Column(nullable = false)
    private String lineName;

    private String startStation;
    private String endStation;

    @Column(nullable = false)
    private int duration;

    @CreationTimestamp
    private LocalDateTime purchasedAt;
}
