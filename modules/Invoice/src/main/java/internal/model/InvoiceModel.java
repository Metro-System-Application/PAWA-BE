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
public class InvoiceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceID;

    @ManyToOne
    @JoinColumn(name="passenger_id", referencedColumnName = "passengerID", nullable = false)
    private Passenger passenger;

    private String email;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime purchasedAt;
}
