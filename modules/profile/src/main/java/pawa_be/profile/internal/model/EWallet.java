package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="Passenger")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID walletID;

    private BigDecimal balance;

    //FK, modify later
    private UUID passengerID;
}
