package pawa_be.cart.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawa_be.cart.internal.dto.AddToCartRequest;
import pawa_be.cart.internal.dto.CartDto;
import pawa_be.cart.internal.dto.CartItemDto;
import pawa_be.cart.internal.model.CartItemModel;
import pawa_be.cart.internal.model.CartModel;
import pawa_be.cart.internal.repository.CartItemRepository;
import pawa_be.cart.internal.repository.CartRepository;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PassengerRepository passengerRepository;
    private final TicketTypeRepository ticketTypeRepository;

    private static final Duration CART_EXPIRY = Duration.ofHours(1); // Items expire after 1 hour

    @Transactional
    public CartDto getOrCreateCart(String passengerId) {
        PassengerModel passenger = findPassenger(passengerId);

        CartModel cart = cartRepository.findByPassengerModel(passenger)
                .orElseGet(() -> createNewCart(passenger));

        return toCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(String passengerId, AddToCartRequest request) {
        PassengerModel passenger = findPassenger(passengerId);
        TicketModel ticketType = findTicketType(request.getTicketType());

        CartModel cart = cartRepository.findByPassengerModel(passenger)
                .orElseGet(() -> createNewCart(passenger));

        // Create and save the cart item
        CartItemModel cartItem = new CartItemModel();
        cartItem.setCart(cart);
        cartItem.setLineID(request.getLineId());
        cartItem.setStartStationID(request.getStartStationId());
        cartItem.setEndStationID(request.getEndStationId());
        cartItem.setType(ticketType);

        cartItemRepository.save(cartItem);

        return toCartDto(cart);
    }

    @Transactional
    public CartDto removeFromCart(String passengerId, UUID cartItemId) {
        PassengerModel passenger = findPassenger(passengerId);
        CartModel cart = findCart(passenger);

        CartItemModel cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Validate that the item belongs to this cart
        if (!cartItem.getCart().getCartID().equals(cart.getCartID())) {
            throw new NotFoundException("Cart item does not belong to this passenger");
        }

        cartItemRepository.deleteById(cartItemId);

        return toCartDto(cart);
    }

    @Transactional
    public void clearCart(String passengerId) {
        PassengerModel passenger = findPassenger(passengerId);
        CartModel cart = findCart(passenger);

        cartItemRepository.deleteByCart(cart);
    }

    public BigDecimal calculateTotalPrice(String passengerId) {
        CartDto cartDto = getOrCreateCart(passengerId);
        return cartDto.getTotalPrice();
    }

    private PassengerModel findPassenger(String passengerId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);
        if (passenger == null) {
            throw new NotFoundException("Passenger not found with ID: " + passengerId);
        }
        return passenger;
    }

    private CartModel findCart(PassengerModel passenger) {
        return cartRepository.findByPassengerModel(passenger)
                .orElseThrow(() -> new NotFoundException("Cart not found for passenger"));
    }

    private TicketModel findTicketType(pawa_be.ticket.external.enumerator.TicketType ticketType) {
        return ticketTypeRepository.findById(ticketType)
                .orElseThrow(() -> new NotFoundException("Ticket type not found: " + ticketType));
    }

    private CartModel createNewCart(PassengerModel passenger) {
        CartModel newCart = new CartModel();
        newCart.setPassengerModel(passenger);
        return cartRepository.save(newCart);
    }

    private CartDto toCartDto(CartModel cart) {
        List<CartItemModel> items = cartItemRepository.findByCart(cart);

        LocalDateTime now = LocalDateTime.now();
        items.removeIf(item -> item.getCreatedAt().plus(CART_EXPIRY).isBefore(now));

        List<CartItemDto> itemDtos = items.stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemDtos.stream()
                .map(CartItemDto::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .cartId(cart.getCartID())
                .passengerId(cart.getPassengerModel().getPassengerID())
                .items(itemDtos)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .totalPrice(totalPrice)
                .build();
    }

    private CartItemDto toCartItemDto(CartItemModel item) {
        return CartItemDto.builder()
                .cartItemId(item.getCartItemID())
                .lineId(item.getLineID())
                // Station names would ideally come from an integration with a metro line
                // service
                .startStationId(item.getStartStationID())
                .endStationId(item.getEndStationID())
                .ticketType(item.getType().getTicketType())
                .ticketTypeName(item.getType().getDisplayName())
                .price(item.getType().getPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
