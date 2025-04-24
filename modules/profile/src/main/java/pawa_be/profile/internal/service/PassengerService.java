package pawa_be.profile.internal.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.internal.dto.RequestUpdatePassengerDTO;
import pawa_be.profile.internal.dto.ResponsePassengerDTO;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PassengerService {
    @Autowired
    private final PassengerRepository passengerRepository;

    public void updateCurrentPassengerById(String passengerId, @Valid RequestUpdatePassengerDTO updatedInfo) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);

        if (updatedInfo.getPassengerPhone() != null) {
            passenger.setPassengerPhone(updatedInfo.getPassengerPhone());
        }

        if (updatedInfo.getPassengerAddress() != null) {
            passenger.setPassengerAddress(updatedInfo.getPassengerAddress());
        }

        passenger.setUpdatedAt(LocalDateTime.now());
        passengerRepository.save(passenger);
    }

    public ResponsePassengerDTO getCurrentPassengerById(String passengerId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);

        return new ResponsePassengerDTO(
                passenger.getPassengerFirstName(),
                passenger.getPassengerMiddleName(),
                passenger.getPassengerLastName(),
                passenger.getPassengerPhone(),
                passenger.getPassengerAddress(),
                passenger.getPassengerDateOfBirth(),
                passenger.getNationalID(),
                passenger.getStudentID(),
                passenger.getHasDisability(),
                passenger.getIsRevolutionary()
        );
    }
}
