package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "TopUpTransaction")
public class TopUpTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionalID;
    private BigDecimal amount;

    // FK, modify later
    private UUID walletID;
}
