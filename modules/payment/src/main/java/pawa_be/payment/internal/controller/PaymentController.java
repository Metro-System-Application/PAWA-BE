package pawa_be.payment.internal.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import pawa_be.insfrastructure.stripe.dto.ResponseCreateStripeSessionDTO;
import pawa_be.payment.internal.dto.RequestPayCheckoutWithStripeDTO;
import pawa_be.payment.internal.dto.RequestPurchaseTicketForPassengerDTO;
import pawa_be.payment.internal.dto.RequestTopUpBalanceDTO;
import pawa_be.payment.internal.service.PaymentService;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResult;
import pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResultType;

import java.math.BigDecimal;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getEmailFromAuthentication;
import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;
import static pawa_be.payment.internal.service.result.PurchaseTicketForPassengerWithIdByOperatorResultType.INSUFFICIENT_BALANCE;

@RestController
@RequestMapping("/payment")
@Tag(name = "Payment Controller", description = "Operations related to e-wallet payments and ticket purchases")
@RequiredArgsConstructor
class PaymentController {
    @Autowired
    private final PaymentService paymentService;

    @Operation(
            summary = "Purchase ticket(s) for passenger",
            description = "Processes a ticket purchase request for a given passenger ID. Requires sufficient e-wallet balance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket purchase successful",
                    content = @Content(
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    summary = "Successful Purchase",
                                    value = """
                                        {
                                            "success": true,
                                            "message": "Purchase was successful",
                                            "data": {"remainingBalance": 10000}
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Payment required - insufficient balance",
                    content = @Content(
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "InsufficientBalance",
                                    summary = "Insufficient Balance",
                                    value = """
                                        {
                                            "success": false,
                                            "message": "Insufficient balance",
                                            "data": null
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - invalid ticket data",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "BadRequest",
                                    summary = "Invalid ticket data",
                                    value = """
                                        {
                                            "success": false,
                                            "message": "Missing ticket type or quantity",
                                            "data": null
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Unexpected error",
                                    value = """
                                        {
                                            "success": false,
                                            "message": "An unexpected error occurred. Please try again later.",
                                            "data": null
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/purchase-ticket/{passengerId}")
    ResponseEntity<GenericResponseDTO<?>> purchaseTicketForPassengerWithId(
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Top-up successful",
                    content = @Content(
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "TopUpSuccess",
                                    summary = "Successful top-up",
                                    value = """
                                        {
                                            "success": true,
                                            "message": "Top up was successful",
                                            "data": 300.00
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - user not authenticated",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    summary = "User not logged in",
                                    value = """
                                        {
                                            "success": false,
                                            "message": "Unauthorized - please log in",
                                            "data": null
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Unexpected error",
                                    value = """
                                        {
                                            "success": false,
                                            "message": "An unexpected error occurred. Please try again later.",
                                            "data": null
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/top-up-balance/{amount}")
    ResponseEntity<GenericResponseDTO<?>> topUpBalance_DELETE(
            @Parameter(description = "Amount to top up in the wallet") @PathVariable int amount,
            @Parameter(hidden = true) Authentication authentication) {

        // TODO: DELETE: For testing only
        String passengerId = getUserIdFromAuthentication(authentication);
        BigDecimal balance = paymentService.selfTopUpEWallet(passengerId, amount);
        return ResponseEntity
                .ok()
                .body(new GenericResponseDTO<>(
                        true,
                        "Top up was successful",
                        balance));
    }

    @PostMapping("/top-up-balance")
    ResponseEntity<GenericResponseDTO<?>> topUpBalance(
            @Parameter(hidden = true) Authentication authentication,
            @Valid RequestTopUpBalanceDTO topUpBalanceDTO
            ) throws StripeException {
        String passengerId = getUserIdFromAuthentication(authentication);
        String passengerEmail = getEmailFromAuthentication(authentication);

        ResponseCreateStripeSessionDTO response =  paymentService.createTopUpPaymentSession(passengerId, passengerEmail, topUpBalanceDTO.getPrice(), topUpBalanceDTO.getSuccessUrl(), topUpBalanceDTO.getCancelUrl());
        return ResponseEntity
                .status(HttpStatus.PERMANENT_REDIRECT)
                .body(new GenericResponseDTO<>(
                        true,
                        "",
                        response));
    }

    @PostMapping("/checkout")
    ResponseEntity<GenericResponseDTO<?>> checkout(
            @Parameter(hidden = true) Authentication authentication,
            @Valid RequestPayCheckoutWithStripeDTO payCheckoutWithStripeDTO
    ) throws StripeException {
        String passengerId = getUserIdFromAuthentication(authentication);
        String passengerEmail = getEmailFromAuthentication(authentication);

        ResponseCreateStripeSessionDTO response =  paymentService.createTicketPaymentSession(passengerId, passengerEmail, payCheckoutWithStripeDTO.getSuccessUrl(), payCheckoutWithStripeDTO.getCancelUrl());
        return ResponseEntity
                .status(HttpStatus.PERMANENT_REDIRECT)
                .body(new GenericResponseDTO<>(
                        true,
                        "",
                        response));
    }

    @PostMapping("/success")
    ResponseEntity<GenericResponseDTO<?>> handlePaymentSuccess(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws StripeException {
//        System.out.printf("Stripe payment success. Session ID: %s%n. signature: %s", payload, sigHeader);

        paymentService.processSuccessfulTopUp(payload);

        return ResponseEntity.ok(new GenericResponseDTO<>(
                true,
                "Payment successful",
                null
        ));
    }
}
