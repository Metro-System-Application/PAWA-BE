package pawa_be.payment.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.profile.internal.model.PassengerModel;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="e_wallet")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EwalletModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID walletID;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "passenger_id", referencedColumnName = "passengerID", nullable = false)
    private PassengerModel passengerModel;
}
