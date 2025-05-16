package pawa_be.cart.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pawa_be.cart.internal.dto.AddToCartRequest;
import pawa_be.cart.internal.dto.CartDto;
import pawa_be.cart.internal.dto.RequestEditCartItemDTO;
import pawa_be.cart.internal.dto.ResponseEditCartItemDTO;
import pawa_be.cart.internal.service.CartService;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;

import java.math.BigDecimal;
import java.util.UUID;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart Controller", description = "Operations for managing shopping cart")
@RequiredArgsConstructor
class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get passenger cart", description = "Retrieves the cart contents for the specified passenger")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    ResponseEntity<GenericResponseDTO<CartDto>> getCart(
            @Parameter(hidden = true) Authentication authentication
    ) {
        String passengerId = getUserIdFromAuthentication(authentication);
        CartDto cart = cartService.getOrCreateCart(passengerId);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Cart retrieved successfully", cart));
    }

    @PostMapping("/item")
    @Operation(summary = "Add item to cart", description = "Adds a new ticket to the passenger's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added to cart"),
            @ApiResponse(responseCode = "404", description = "Passenger not found or ticket type not found")
    })
    ResponseEntity<GenericResponseDTO<CartDto>> addToCart(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        String passengerId = getUserIdFromAuthentication(authentication);
        CartDto cart = cartService.addToCart(passengerId, request);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Item added to cart", cart));
    }

    @DeleteMapping("/item/{cartItemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the passenger's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed from cart"),
            @ApiResponse(responseCode = "404", description = "Passenger or cart item not found")
    })
    ResponseEntity<GenericResponseDTO<CartDto>> removeFromCart(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable UUID cartItemId) {
        String passengerId = getUserIdFromAuthentication(authentication);
        CartDto cart = cartService.removeFromCart(passengerId, cartItemId);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Item removed from cart", cart));
    }

    @PutMapping("/item")
    @Operation(summary = "Edit item in cart", description = "Edits a specific item in the passenger's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item edited in the cart"),
            @ApiResponse(responseCode = "404", description = "Passenger or cart item not found")
    })
    ResponseEntity<GenericResponseDTO<ResponseEditCartItemDTO>> updateItemInCart(
            @Parameter(hidden = true) Authentication authentication,
            @Valid RequestEditCartItemDTO requestEditCartItemDTO) {
        String passengerId = getUserIdFromAuthentication(authentication);
        ResponseEditCartItemDTO response = cartService.editCartItem(passengerId, requestEditCartItemDTO);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Item updated in cart", response));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the passenger's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    ResponseEntity<GenericResponseDTO<Void>> clearCart(
            @Parameter(hidden = true) Authentication authentication
    ) {
        String passengerId = getUserIdFromAuthentication(authentication);
        cartService.clearCart(passengerId);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Cart cleared successfully", null));
    }

    @GetMapping("/price")
    @Operation(summary = "Calculate total price", description = "Calculates the total price of all items in the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    ResponseEntity<GenericResponseDTO<BigDecimal>> getTotalPrice(
            @Parameter(hidden = true) Authentication authentication
    ) {
        String passengerId = getUserIdFromAuthentication(authentication);
        BigDecimal totalPrice = cartService.calculateTotalPrice(passengerId);
        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Total price calculated", totalPrice));
    }

    @GetMapping("/checkout/{passengerId}")
    @Operation(summary = "Proceed to checkout", description = "Start the checkout process for the items in the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout initiated"),
            @ApiResponse(responseCode = "404", description = "Passenger not found or empty cart")
    })
    ResponseEntity<GenericResponseDTO<String>> proceedToCheckout(@PathVariable String passengerId) {
        // This is a placeholder for the checkout process
        // In a real implementation, this would redirect to or integrate with the
        // payment module
        CartDto cart = cartService.getOrCreateCart(passengerId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return ResponseEntity.ok(
                    new GenericResponseDTO<>(false, "Cart is empty", "Cannot proceed to checkout with an empty cart"));
        }

        return ResponseEntity.ok(
                new GenericResponseDTO<>(true, "Checkout initiated",
                        "Redirecting to payment for " + cart.getItems().size() + " items totaling "
                                + cart.getTotalPrice()));
    }
}
