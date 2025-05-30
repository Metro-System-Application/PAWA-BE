package pawa_be.payment.internal.service;

import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.Product;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.cart.external.dto.ResponseGetCartContentsDTO;
import pawa_be.cart.external.service.IExternalCartService;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.insfrastructure.stripe.dto.*;
import pawa_be.insfrastructure.stripe.service.IStripeService;
import pawa_be.payment.internal.dto.*;
import pawa_be.payment.internal.model.EwalletModel;
import pawa_be.payment.internal.model.TopUpTransactionModel;
import pawa_be.payment.internal.repository.EWalletRepository;
import pawa_be.payment.internal.repository.TopUpTransactionRepository;
import pawa_be.payment.internal.service.result.PurchaseWithEwalletResult;
import pawa_be.payment.internal.service.result.PurchaseWithEWalletResultType;
import pawa_be.profile.external.service.IExternalPassengerService;
import pawa_be.ticket.external.service.IExternalTicketService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    IExternalTicketService externalTicketService;

    @Autowired
    IExternalPassengerService externalPassengerService;

    @Autowired
    TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    EWalletRepository eWalletRepository;

    @Autowired
    IStripeService stripeService;

    @Autowired
    IExternalCartService externalCartService;

    @Autowired
    InvoiceService invoiceService;

    public PurchaseWithEwalletResult purchaseTicketForPassengerWithEmailByOperator(
            String passengerEmail,
            RequestPurchaseTicketForPassengerByOperatorDTO requestPurchaseTicketForPassengerDTO) {
        String passengerId = externalPassengerService.findPassengerIdByEmail(passengerEmail)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Passenger with email '%s' not found", passengerEmail)
                ));

        EwalletModel passengerEwallet = eWalletRepository
                .findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Passenger with ID '%s' not found", passengerId)
                        )
                );

        final List<RequestPurchaseTicketForPassengerTicketDTO> tickets =
                requestPurchaseTicketForPassengerDTO.getTickets();

        for (RequestPurchaseTicketForPassengerTicketDTO ticket : tickets) {
            Pair<Boolean, String> eligibility = externalTicketService.checkEligibleTicketType(ticket.getTicketType(), passengerId);
            if (!eligibility.getLeft()) {
                throw new IllegalArgumentException(eligibility.getRight());
            }
        }

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

        List<CartItemForInvoiceDTO> detailedItems = tickets
                .stream()
                .map(item -> new CartItemForInvoiceDTO(
                                item.getTicketType().name(),
                                externalTicketService.getTicketPriceByEnum(item.getTicketType()),
                                item.getAmount(),
                                item.getLineID(),
                                item.getLineName(),
                                item.getStartStation(),
                                item.getEndStation()
                        )
                ).toList();

        invoiceService.createInvoice(
                new RequestCreateInvoiceDTO(
                        passengerId,
                        "example@example.com", // TODO: remove from here ??
                        detailedItems
                ),
                null
        );

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

    public ResponseCreateStripeSessionDTO createTicketPaymentSessionFromCart(String userId, String email, String successUrl, String cancelUrl) throws StripeException {
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
                                item.getQuantity(),
                                item.getTicketType().name(),
                                item.getLineID(),
                                item.getLineName(),
                                item.getStartStation(),
                                item.getEndStation()
                        ))
                        .toList(), true);
    }

    public ResponseCreateStripeSessionDTO createTicketPaymentSession(String userId, String email, String successUrl, String cancelUrl, List<RequestPurchaseTicketForPassengerTicketDTO> tickets) throws StripeException {
        RequestPaymentDataDTO paymentCredentialsDTO = new RequestPaymentDataDTO(userId, email);
        RequestRedirectUrlsDTO redirectData = new RequestRedirectUrlsDTO(successUrl, cancelUrl);

        for (RequestPurchaseTicketForPassengerTicketDTO ticket : tickets) {
            Pair<Boolean, String> eligibility = externalTicketService.checkEligibleTicketType(ticket.getTicketType(), userId);
            if (!eligibility.getLeft()) {
                throw new IllegalArgumentException(eligibility.getRight());
            }
        }

        return stripeService.createDirectTicketPaymentSession(
                paymentCredentialsDTO,
                redirectData,
                tickets.stream()
                        .map(item -> new LineItemRequestDTO(
                                item.getTicketType().name(),
                                externalTicketService.getTicketPriceByEnum(item.getTicketType()).longValue(),
                                item.getAmount(),
                                item.getTicketType().name(),
                                item.getLineID(),
                                item.getLineName(),
                                item.getStartStation(),
                                item.getEndStation()
                        ))
                        .toList(), false);
    }


    public void processSuccessfulTopUp(String payload) {
        try {
            ResponseProcessSuccessfulTopUpDTO dto = stripeService.processSuccessfulTransaction(payload);

            List<LineItem> lineItems = dto.getLineItems().getData();
            if (dto.isTopUp()) {
                BigDecimal amountInVND = BigDecimal.valueOf(dto.getAmount());

                EwalletModel wallet = eWalletRepository.findByPassengerModel_PassengerID(dto.getUserid())
                        .orElseThrow(() -> new IllegalArgumentException("Ewallet not found for user: " + dto.getUserid()));

                TopUpTransactionModel transaction = new TopUpTransactionModel();
                transaction.setStripeId(dto.getTransactionId());
                transaction.setAmount(amountInVND);
                transaction.setEwallet(wallet);

                topUpTransactionRepository.save(transaction);

                wallet.setBalance(wallet.getBalance().add(amountInVND));
                eWalletRepository.save(wallet);
            } else {
                List<CartItemForInvoiceDTO> detailedItems = lineItems.stream().map(lineItem -> {
                    Product product = lineItem.getPrice().getProductObject();
                    Map<String, String> metadata = product.getMetadata();

                    return new CartItemForInvoiceDTO(
                            metadata.get("ticket_type"),
                            BigDecimal.valueOf(lineItem.getAmountTotal()),
                            lineItem.getQuantity(),
                            metadata.get("line_id"),
                            metadata.get("line_name"),
                            metadata.get("start_station"),
                            metadata.get("end_station")
                    );
                }).toList();

                invoiceService.createInvoice(
                        new RequestCreateInvoiceDTO(
                                dto.getUserid(),
                                dto.getUserEmail(),
                                detailedItems
                        ),
                        dto.getTransactionId()
                );

                if (dto.isFromCart()) {
                    externalCartService.cleanCart(dto.getUserid());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public PurchaseWithEwalletResult payForCheckoutWithEWallet(String passengerId, String email) {
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

        final BigDecimal remainingBalance = balance.subtract(price);
        passengerEwallet.setBalance(remainingBalance);

        eWalletRepository.save(passengerEwallet);

        List<CartItemForInvoiceDTO> detailedItems = cartContents
                .getCartContents()
                .stream()
                .map(item -> new CartItemForInvoiceDTO(
                        item.getTicketType().name(),
                        item.getAmountInVND(),
                        item.getQuantity(),
                        item.getLineID(),
                        item.getLineName(),
                        item.getStartStation(),
                        item.getEndStation()
                        )
                    ).toList();

        invoiceService.createInvoice(
                new RequestCreateInvoiceDTO(
                        passengerId,
                        email,
                        detailedItems
                ),
                null
        );

        externalCartService.cleanCart(passengerId);

        return new PurchaseWithEwalletResult(
                PurchaseWithEWalletResultType.SUCCESS,
                new ResponsePurchaseTicketForPassengerDTO(remainingBalance)
        );
    }

    public ResponseGetBalanceDTO getBalanceByID(String passengerId) {
        EwalletModel passengerEwallet = eWalletRepository
                .findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Passenger with ID '%s' not found", passengerId)
                        )
                );

        return new ResponseGetBalanceDTO(passengerEwallet.getBalance());
    }
}

