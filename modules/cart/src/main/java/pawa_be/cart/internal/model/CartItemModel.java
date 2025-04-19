package pawa_be.cart.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import pawa_be.ticket.internal.model.TicketModel;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="cart_item")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cartItemID;

    @Column(nullable = false)
    private UUID lineID;

    @Column(nullable = false)
    private UUID startStationID;

    @Column(nullable = false)
    private UUID endStationID;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name="cart_id", referencedColumnName = "cartID", nullable = false)
    private CartModel cart;

    @ManyToOne
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    private TicketModel type;
}
