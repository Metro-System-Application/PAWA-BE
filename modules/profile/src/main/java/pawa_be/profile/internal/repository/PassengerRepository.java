package pawa_be.profile.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.profile.internal.model.Passenger;

import java.util.List;

@Repository
public interface PassengerRepository extends CrudRepository<Passenger, String> {

}
