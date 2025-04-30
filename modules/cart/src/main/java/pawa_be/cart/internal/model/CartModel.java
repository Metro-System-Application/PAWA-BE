package pawa_be.cart.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pawa_be.profile.internal.model.PassengerModel;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="cart")
@Data
public class CartModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cartID;

    @OneToOne
    @JoinColumn(name="passenger_id", referencedColumnName = "passengerID", nullable = false, insertable = false, updatable = false)
    private PassengerModel passengerModel;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
