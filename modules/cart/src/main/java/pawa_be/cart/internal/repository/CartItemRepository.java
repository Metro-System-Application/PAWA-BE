package pawa_be.cart.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.cart.internal.model.CartItemModel;
import pawa_be.cart.internal.model.CartModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends CrudRepository<CartItemModel, UUID> {
    List<CartItemModel> findByCart(CartModel cart);

    void deleteByCart(CartModel cart);

    void deleteByCartAndCartItemID(CartModel cart, UUID cartItemID);
}
