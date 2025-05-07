package pawa_be.profile.external.service;

import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.internal.model.PassengerModel;

public interface IExternalPassengerService {
    PassengerModel registerPassenger(String userId, RequestRegisterPassengerDTO dto);
    boolean checkIsPassengerProfileIsFilled(String userId);
}