package pawa_be.payment.internal.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.insfrastructure.stripe.dto.*;
import pawa_be.insfrastructure.stripe.service.IStripeService;
import pawa_be.payment.internal.dto.RequestPurchaseTicketForPassengerDTO;
import pawa_be.payment.internal.dto.RequestPurchaseTicketForPassengerTicketDTO;
import pawa_be.payment.internal.dto.ResponsePurchaseTicketForPassengerDTO;
import pawa_be.payment.internal.model.EwalletModel;
import pawa_be.payment.internal.model.TopUpTransactionModel;
import pawa_be.payment.internal.repository.EWalletRepository;
import pawa_be.payment.internal.repository.TopUpTransactionRepository;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResult;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResultType;
import pawa_be.ticket.external.service.IExternalTicketService;
import pawa_be.ticket.internal.model.TicketModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    IExternalTicketService externalTicketService;

    @Autowired
    TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    EWalletRepository eWalletRepository;

    @Autowired
    IStripeService stripeService;

    public PurchaseTicketForPassengerWithIdByOperatorResult purchaseTicketForPassengerWithIdByOperator(
            String passengerId,
            RequestPurchaseTicketForPassengerDTO requestPurchaseTicketForPassengerDTO) {
        EwalletModel passengerEwallet = eWalletRepository
                .findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Passenger with ID '%s' not found", passengerId)
                        )
                );

        final List<RequestPurchaseTicketForPassengerTicketDTO> tickets =
                requestPurchaseTicketForPassengerDTO.getTickets();

        BigDecimal totalPrice = tickets.stream()
                .map(ticket -> {
                    BigDecimal price = externalTicketService.getTicketPriceByEnum(ticket.getTicketType());
                    return price.multiply(BigDecimal.valueOf(ticket.getAmount()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal balance = passengerEwallet.getBalance();
        final int isEnoughBalance = balance.compareTo(totalPrice);
        if (isEnoughBalance < 0) {
            return new PurchaseTicketForPassengerWithIdByOperatorResult(
                    PurchaseTicketForPassengerWithIdByOperatorResultType.INSUFFICIENT_BALANCE,
                    null
            );
        }

        final BigDecimal remainingBalance = balance.subtract(totalPrice);
        passengerEwallet.setBalance(remainingBalance);

        eWalletRepository.save(passengerEwallet);

        return new PurchaseTicketForPassengerWithIdByOperatorResult(
                PurchaseTicketForPassengerWithIdByOperatorResultType.SUCCESS,
                new ResponsePurchaseTicketForPassengerDTO(remainingBalance)
        );
    }

    public BigDecimal selfTopUpEWallet(String passengerId, int amount) {
        EwalletModel passengerEwallet = eWalletRepository
                .findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Passenger with ID '%s' not found", passengerId)
                        )
                );

        BigDecimal balance = passengerEwallet.getBalance().add(BigDecimal.valueOf(amount));
        passengerEwallet.setBalance(passengerEwallet.getBalance().add(BigDecimal.valueOf(amount)));

        eWalletRepository.save(passengerEwallet);

        return balance;
    }

    public ResponseCreateStripeSessionDTO createTopUpPaymentSession(String userId, String email, Long price, String successUrl, String cancelUrl) throws StripeException {
        RequestPaymentDataDTO paymentCredentialsDTO = new RequestPaymentDataDTO(userId, email, price);
        RequestRedirectUrlsDTO redirectData = new RequestRedirectUrlsDTO(successUrl, cancelUrl);

        return stripeService.createTopUpPaymentSession(paymentCredentialsDTO, redirectData);
    }

    public void processSuccessfulTopUp(String payload) throws StripeException {
        ResponseProcessSuccessfulTopUpDTO dto = stripeService.processSuccessfulTopUp(payload);

        BigDecimal amountInVND = BigDecimal.valueOf(dto.getAmount());

        EwalletModel wallet = eWalletRepository.findByPassengerModel_PassengerID(dto.getUserid())
                .orElseThrow(() -> new IllegalArgumentException("Ewallet not found for user: " + dto.getUserid()));

        TopUpTransactionModel transaction = new TopUpTransactionModel();
        transaction.setAmount(amountInVND);
        transaction.setEwallet(wallet);

        topUpTransactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance().add(amountInVND));
        eWalletRepository.save(wallet);
    }
}

