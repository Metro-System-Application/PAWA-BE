package pawa_be.cart.internal.repository;

import pawa_be.cart.internal.model.CartModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<CartModel, UUID> {
    Optional<CartModel> findByPassengerModel(PassengerModel passengerModel);
}
