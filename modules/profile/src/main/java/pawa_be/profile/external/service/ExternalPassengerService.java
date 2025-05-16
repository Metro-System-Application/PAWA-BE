package pawa_be.profile.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
class ExternalPassengerService implements IExternalPassengerService {
    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public PassengerModel registerPassenger(String userId, RequestRegisterPassengerDTO dto) {
        PassengerModel passenger = new PassengerModel();
        passenger.setPassengerID(userId);
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
//        return new ResponsePassengerDTO(
//                passenger.getPassengerFirstName(),
//                passenger.getPassengerMiddleName(),
//                passenger.getPassengerLastName(),
//                passenger.getPassengerPhone(),
//                passenger.getPassengerAddress(),
//                passenger.getPassengerDateOfBirth(),
//                passenger.getNationalID(),
//                passenger.getStudentID(),
//                passenger.getHasDisability(),
//                passenger.getIsRevolutionary()
//        );
    }

    @Override
    public boolean checkIsPassengerProfileIsFilled(String userId) {
        return passengerRepository.findPassengerModelByPassengerID(userId) != null;
    }

    @Override
    public boolean isStudent(String userId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(userId);
        return passenger != null && passenger.getStudentID() != null && !passenger.getStudentID().isBlank();
    }

    @Override
    public boolean isRevolutionary(String userId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(userId);
        return passenger != null && Boolean.TRUE.equals(passenger.getIsRevolutionary());
    }

    @Override
    public boolean isBelow6orAbove60YearsOld(String userId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(userId);
        if (passenger == null || passenger.getPassengerDateOfBirth() == null) return false;

        int age = Period.between(passenger.getPassengerDateOfBirth(), LocalDate.now()).getYears();
        return age < 6 || age > 60;
    }

    @Override
    public boolean hasDisabilities(String userId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(userId);
        return passenger != null && Boolean.TRUE.equals(passenger.getHasDisability());
    }
}
