package internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.ewallet.internal.model.EwalletModel;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TopUpTransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionID;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "ewallet_id", referencedColumnName = "walletID", nullable = false)
    private EwalletModel ewallet;


}
