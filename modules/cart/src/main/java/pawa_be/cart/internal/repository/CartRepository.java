package pawa_be.cart.internal.repository;

import pawa_be.cart.internal.model.CartModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<CartModel, UUID> {

}
