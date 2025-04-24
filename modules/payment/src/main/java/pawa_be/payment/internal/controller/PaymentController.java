package pawa_be.payment.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.payment.internal.dto.RequestPurchaseTicketForPassengerDTO;
import pawa_be.payment.internal.service.PaymentService;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResult;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResultType;

import java.math.BigDecimal;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;
import static pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResultType.INSUFFICIENT_BALANCE;

@RestController
@RequestMapping("/payment")
@Tag(name = "Payment Controller", description = "Operations about Payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private final PaymentService paymentService;

    @GetMapping("")
    @Operation(summary = "Payment Module", description = "Returns \"Hello, Payment!\" message.")
    public String greet(){
        return "Hello, Payment!";
    }

    @PostMapping("/purchase-ticket/{passengerId}")
    public ResponseEntity<GenericResponseDTO<?>> purchaseTicketForPassengerWithId(
            @PathVariable String passengerId,
            @Valid @RequestBody RequestPurchaseTicketForPassengerDTO requestPurchaseTicketForPassengerDTO) {
        // TODO: Add role handling
        PurchaseTicketForPassengerWithIdByOperatorResult result =
                paymentService.purchaseTicketForPassengerWithIdByOperator(
                        passengerId,
                        requestPurchaseTicketForPassengerDTO
                );

        PurchaseTicketForPassengerWithIdByOperatorResultType resultType = result.getStatus();
        if (resultType == INSUFFICIENT_BALANCE) {
            return ResponseEntity
                    .status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Insufficient balance",
                            null));
        }

        return ResponseEntity
                .ok()
                .body(new GenericResponseDTO<>(
                        true,
                        "Purchase was successful",
                        result.getRemainingBalance()));
    }

    @GetMapping("/top-up-balance/{amount}")
    public ResponseEntity<GenericResponseDTO<?>> topUpBalance(
            @PathVariable int amount, Authentication authentication) {
        // TODO: remove entire endpoint, for testing only
        String passengerId = getUserIdFromAuthentication(authentication);
        BigDecimal balance = paymentService.selfTopUpEWallet(passengerId, amount);
        return ResponseEntity
                .ok()
                .body(new GenericResponseDTO<>(
                        true,
                        "Top up was successful",
                        balance));
    }
}
