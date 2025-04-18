package internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pawa_be.profile.internal.model.Passenger;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cartID;

    @OneToOne
    @JoinColumn(name="passenger_id", referencedColumnName = "passengerID", nullable = false)
    private Passenger passenger;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
