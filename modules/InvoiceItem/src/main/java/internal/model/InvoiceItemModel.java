package internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @Column(nullable = false)
    private String ticketName;

    @Column(nullable = false)
    private String ticketType;

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private UUID lineID;

    @Column(nullable = false)
    private String lineName;


    private String startStation;
    private String endStation;

    @Column(nullable = false)
    private int duration;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime purchasedAt;
}
