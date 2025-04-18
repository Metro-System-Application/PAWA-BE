package internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="e_wallet")
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name="cart_id", referencedColumnName = "cartID", nullable = false)
    private CartModel cart;

    @ManyToOne
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    private TypeModel type;


}
