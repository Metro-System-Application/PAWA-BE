package pawa_be.payment.internal.service;

import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.cart.external.dto.ResponseGetCartContentsDTO;
import pawa_be.cart.external.service.IExternalCartService;
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
import pawa_be.payment.internal.service.result.PurchaseWithEwalletResult;
import pawa_be.payment.internal.service.result.PurchaseWithEWalletResultType;
import pawa_be.ticket.external.service.IExternalTicketService;

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

    @Autowired
    IExternalCartService externalCartService;

    public PurchaseWithEwalletResult purchaseTicketForPassengerWithIdByOperator(
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

        // TODO: add tickets to the user + create invoices
        final BigDecimal balance = passengerEwallet.getBalance();
        final int isEnoughBalance = balance.compareTo(totalPrice);
        if (isEnoughBalance < 0) {
            return new PurchaseWithEwalletResult(
                    PurchaseWithEWalletResultType.INSUFFICIENT_BALANCE,
                    null
            );
        }

        final BigDecimal remainingBalance = balance.subtract(totalPrice);
        passengerEwallet.setBalance(remainingBalance);

        eWalletRepository.save(passengerEwallet);

        return new PurchaseWithEwalletResult(
                PurchaseWithEWalletResultType.SUCCESS,
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
        RequestPaymentDataDTO paymentCredentialsDTO = new RequestPaymentDataDTO(userId, email);
        RequestRedirectUrlsDTO redirectData = new RequestRedirectUrlsDTO(successUrl, cancelUrl);

        return stripeService.createTopUpPaymentSession(paymentCredentialsDTO, price, redirectData);
    }

    public ResponseCreateStripeSessionDTO createTicketPaymentSession(String userId, String email, String successUrl, String cancelUrl) throws StripeException {
        RequestPaymentDataDTO paymentCredentialsDTO = new RequestPaymentDataDTO(userId, email);
        RequestRedirectUrlsDTO redirectData = new RequestRedirectUrlsDTO(successUrl, cancelUrl);
        ResponseGetCartContentsDTO cartContents = externalCartService.getCartContents(userId);

        return stripeService.createDirectTicketPaymentSession(
                paymentCredentialsDTO,
                redirectData,
                cartContents.getCartContents().stream()
                        .map(item -> new LineItemRequestDTO(
                                item.getName(),
                                item.getAmountInVND().longValue(),
                                item.getQuantity()
                        ))
                        .toList());
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

    public PurchaseWithEwalletResult payForCheckoutWithEWallet(String passengerId) {
        EwalletModel passengerEwallet = eWalletRepository
                .findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Passenger with ID '%s' not found", passengerId)
                        )
                );

        ResponseGetCartContentsDTO cartContents = externalCartService.getCartContents(passengerId);
        BigDecimal price = cartContents.getCartContents().stream().map(ticket -> {
                    BigDecimal curPrice = ticket.getAmountInVND();
                    return curPrice.multiply(BigDecimal.valueOf(ticket.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal balance = passengerEwallet.getBalance();
        final int isEnoughBalance = balance.compareTo(price);
        if (isEnoughBalance < 0) {
            return new PurchaseWithEwalletResult(
                    PurchaseWithEWalletResultType.INSUFFICIENT_BALANCE,
                    null
            );
        }

        final BigDecimal remainingBalance = balance.subtract(balance);
        passengerEwallet.setBalance(remainingBalance);

        // TODO: add tickets to the user + create invoices
        eWalletRepository.save(passengerEwallet);

        return new PurchaseWithEwalletResult(
                PurchaseWithEWalletResultType.SUCCESS,
                new ResponsePurchaseTicketForPassengerDTO(remainingBalance)
        );
    }
}

