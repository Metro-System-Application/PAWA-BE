package pawa_be.payment.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import pawa_be.infrastructure.common.validation.StripeIdEncryptor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="top_up_transaction")
@Data
public class TopUpTransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionID;

    @Convert(converter = StripeIdEncryptor.class)
    private String stripeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "ewallet_id", referencedColumnName = "walletID", nullable = false, updatable = false)
    private EwalletModel ewallet;
}
