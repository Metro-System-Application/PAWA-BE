package pawa_be.profile.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.UUID;

@Repository
public interface PassengerRepository extends CrudRepository<PassengerModel, UUID> {
    PassengerModel findPassengerModelByPassengerID(String passengerID);
    
    // Added email finder method to replace phone lookup
    PassengerModel findPassengerModelByPassengerEmail(String email);
    
    // Keeping phone lookup for backward compatibility
    PassengerModel findPassengerModelByPassengerPhone(String phone);
}
