package pawa_be.profile.internal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.UUID;

@Repository
public interface PassengerRepository extends CrudRepository<PassengerModel, String> {
    PassengerModel findPassengerModelByPassengerID(String passengerID);

    // Keeping phone lookup for backward compatibility
    PassengerModel findPassengerModelByPassengerPhone(String phone);
    
    // Get passenger by email from user_auth table
    @Query(value = "SELECT p.* FROM passenger p " +
                  "JOIN user_auth u ON p.passengerid = u.user_id " +
                  "WHERE u.email = :email", nativeQuery = true)
    PassengerModel findPassengerModelByEmail(@Param("email") String email);
}
