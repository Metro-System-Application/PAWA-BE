package pawa_be.payment.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.model.EwalletModel;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.Optional;

@Repository
public interface EWalletRepository extends CrudRepository<EwalletModel, String> {
    Optional<EwalletModel> findByPassengerModel_PassengerID(String passengerId);
}
