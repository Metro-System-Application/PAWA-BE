package pawa_be.payment.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pawa_be.infrastructure.common.validation.StripeIdEncryptor;
import pawa_be.profile.internal.model.PassengerModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Data
public class InvoiceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoiceid")
    private UUID invoiceId;

    @Convert(converter = StripeIdEncryptor.class)
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "passenger_id", referencedColumnName = "passengerId", updatable = false)
    private PassengerModel passengerModel;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    // Since invoices are created after payment, they are purchased at creation time
    @CreationTimestamp
    private LocalDateTime purchasedAt;
}
