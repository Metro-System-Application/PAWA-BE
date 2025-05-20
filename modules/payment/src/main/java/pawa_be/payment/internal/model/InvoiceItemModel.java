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
    @EmbeddedId
    private InvoiceItemKey id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "invoiceId", nullable = false, updatable = false)
    private InvoiceModel invoiceModel;

    @Column(nullable = false)
    private String ticketType;

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;

    @Column(name = "line_id", nullable = false)
    private String lineId;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Column(name = "start_station")
    private String startStation;

    @Column(name = "end_station")
    private String endStation;

    @Column(nullable = false)
    private int duration;

    @CreationTimestamp
    private LocalDateTime purchasedAt;

    public TicketStatus getStatus() {
        return id.getStatus();
    }

    public UUID getInvoiceItemId() {
        return id.getInvoiceItemId();
    }

    public void setStatus(TicketStatus status) {
        if (this.id == null) {
            this.id = new InvoiceItemKey();
            this.id.setInvoiceItemId(UUID.randomUUID());
        }
        this.id.setStatus(status);
    }
}
