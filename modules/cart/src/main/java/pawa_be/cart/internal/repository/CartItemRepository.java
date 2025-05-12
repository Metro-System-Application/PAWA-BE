package pawa_be.cart.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.cart.internal.model.CartItemModel;
import pawa_be.cart.internal.model.CartModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends CrudRepository<CartItemModel, UUID> {
    List<CartItemModel> findByCart(CartModel cart);
    List<CartItemModel> findAllByCart_PassengerModel_PassengerID(String passengerID);
    Optional<CartItemModel> findByCart_CartIDAndCartItemID(UUID cartID, UUID cartItemID);
    void deleteByCart(CartModel cart);
    void deleteByCartAndCartItemID(CartModel cart, UUID cartItemID);
}
