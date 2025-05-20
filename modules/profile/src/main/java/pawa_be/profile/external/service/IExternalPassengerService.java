package pawa_be.profile.external.service;

import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.Optional;

public interface IExternalPassengerService {
    PassengerModel registerPassenger(String userId, RequestRegisterPassengerDTO dto);
    Optional<String> findPassengerIdByEmail(String email);
    boolean checkIsPassengerProfileIsFilled(String userId);
    boolean isStudent(String userId);
    boolean isRevolutionary(String userId);
    boolean isBelow6orAbove60YearsOld(String userId);
    boolean hasDisabilities(String userId);
    Optional<ResponsePassengerDTO> getPassengerByEmail(String email);
}