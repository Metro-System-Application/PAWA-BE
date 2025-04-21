package pawa_be.profile.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;

import java.time.LocalDateTime;

@Service
public class ExternalPassengerService implements IExternalPassengerService {
    @Autowired
    private PassengerRepository passengerRepository;

    // TODO: email field has to be removed
    @Override
    public PassengerModel registerPassenger(String userId, String email, RequestRegisterPassengerDTO dto) {
        PassengerModel passenger = new PassengerModel();
        passenger.setPassengerID(userId);
        // TODO: to be removed
        passenger.setPassengerEmail(email);
        passenger.setPassengerFirstName(dto.getPassengerFirstName());
        passenger.setPassengerMiddleName(dto.getPassengerMiddleName());
        passenger.setPassengerLastName(dto.getPassengerLastName());
        passenger.setPassengerPhone(dto.getPassengerPhone());
        passenger.setPassengerAddress(dto.getPassengerAddress());
        passenger.setPassengerDateOfBirth(dto.getPassengerDateOfBirth());
        passenger.setHasDisability(Boolean.TRUE.equals(dto.getHasDisability()));
        passenger.setIsRevolutionary(Boolean.TRUE.equals(dto.getIsRevolutionary()));
        passenger.setNationalID(dto.getNationalID());
        passenger.setStudentID(dto.getStudentID());
        passenger.setCreatedAt(LocalDateTime.now());
        passenger.setUpdatedAt(LocalDateTime.now());

        passengerRepository.save(passenger);
        return passenger;
    }

    @Override
    public void updatePassengerByEmail(String oldEmail, String newEmail) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerEmail(oldEmail);
        passenger.setPassengerEmail(newEmail);
        passenger.setUpdatedAt(LocalDateTime.now());
        passengerRepository.save(passenger);
    }
}
