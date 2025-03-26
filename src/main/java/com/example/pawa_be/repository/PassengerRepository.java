package com.example.pawa_be.repository;

import com.example.pawa_be.model.Passenger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends CrudRepository<Passenger, String> {
    List<Passenger> findPasssengerByPassengerEmail(String passengerEmail);
    Passenger findById(long id);
    void deleteById(long id);
}
