package pawa_be.payment.external.service;

import pawa_be.profile.internal.model.PassengerModel;

public interface IExternalPaymentService {
    void createPassengerEwallet(PassengerModel passengerModel);
}
