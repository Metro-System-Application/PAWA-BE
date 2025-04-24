package pawa_be.payment.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Payment Controller", description = "Operations related to e-wallet payments and ticket purchases")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private final PaymentService paymentService;

    @Operation(summary = "Test endpoint", description = "Returns a simple greeting message for the Payment module.")
    @GetMapping("")
    public String greet(){
        return "Hello, Payment!";
    }

    @Operation(
            summary = "Purchase ticket(s) for passenger",
            description = "Processes a ticket purchase request for a given passenger ID. Requires sufficient e-wallet balance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket purchase successful",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "402", description = "Payment required - insufficient balance",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid ticket data",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/purchase-ticket/{passengerId}")
    public ResponseEntity<GenericResponseDTO<?>> purchaseTicketForPassengerWithId(
            @Parameter(description = "Passenger ID of the person purchasing the ticket") @PathVariable String passengerId,
            @Valid @RequestBody
            @Parameter(description = "Details of the ticket(s) to be purchased")
            RequestPurchaseTicketForPassengerDTO requestPurchaseTicketForPassengerDTO) {

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

    @Operation(
            summary = "Top-up e-wallet balance (for testing only)",
            description = "Increases the e-wallet balance for the currently authenticated passenger by the specified amount."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top-up successful",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/top-up-balance/{amount}")
    public ResponseEntity<GenericResponseDTO<?>> topUpBalance(
            @Parameter(description = "Amount to top up in the wallet") @PathVariable int amount,
            @Parameter(hidden = true) Authentication authentication) {

        // ⚠️ For testing only
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
