package pawa_be.payment.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.payment.internal.model.EwalletModel;
import pawa_be.payment.internal.repository.EWalletRepository;
import pawa_be.profile.internal.model.PassengerModel;

import java.math.BigDecimal;

@Service
public class ExternalPaymentService implements IExternalPaymentService {
    @Autowired
    EWalletRepository walletRepository;

    @Override
    public void createPassengerEwallet(PassengerModel passengerModel) {
        EwalletModel ewalletModel = new EwalletModel();
        ewalletModel.setPassengerModel(passengerModel);
        ewalletModel.setBalance(BigDecimal.valueOf(0));

        walletRepository.save(ewalletModel);
    }
}
