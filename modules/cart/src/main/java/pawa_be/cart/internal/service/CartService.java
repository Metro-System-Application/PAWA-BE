package pawa_be.cart.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawa_be.cart.internal.dto.*;
import pawa_be.cart.internal.model.CartItemModel;
import pawa_be.cart.internal.model.CartModel;
import pawa_be.cart.internal.repository.CartItemRepository;
import pawa_be.cart.internal.repository.CartRepository;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.service.IExternalTicketService;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    IExternalTicketService externalTicketService;

    private static final Duration CART_EXPIRY = Duration.ofDays(7); // Items expire after 1 week

    @Transactional
    public CartDto getOrCreateCart(String passengerId) {
        CartModel cart = cartRepository.findByPassengerModel_PassengerID(passengerId)
                .orElseGet(() -> createNewCart(passengerId));

        return toCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(String passengerId, AddToCartRequest request) {
        TicketModel ticketType = findTicketType(request.getTicketType());

        Pair<Boolean, String> eligibility = externalTicketService.checkEligibleTicketType(ticketType.getTicketType(), passengerId);
        if (!eligibility.getLeft()) {
            throw new IllegalArgumentException(eligibility.getRight());
        }

        CartModel cart = cartRepository.findByPassengerModel_PassengerID(passengerId)
                .orElseGet(() -> createNewCart(passengerId));

        Optional<CartItemModel> optionalCartItem = cartItemRepository.findByCart_CartIDAndType(
                cart.getCartID(), ticketType);

        CartItemModel cartItem;
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setAmount(cartItem.getAmount() + 1);
        } else {
            cartItem = new CartItemModel();
            cartItem.setCart(cart);
            cartItem.setLineID(request.getLineId());
            cartItem.setAmount(1);
            cartItem.setStartStationID(request.getStartStationId());
            cartItem.setEndStationID(request.getEndStationId());
            cartItem.setType(ticketType);
        }

        cartItemRepository.save(cartItem);
        return toCartDto(cart);
    }


    @Transactional
    public CartDto removeFromCart(String passengerId, UUID cartItemId) {
        CartModel cart = findCart(passengerId);

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
        CartModel cart = findCart(passengerId);

        cartItemRepository.deleteByCart(cart);
    }

    public ResponseEditCartItemDTO editCartItem(String passengerId, RequestEditCartItemDTO requestEditCartItemDTO) {
        CartModel cart = cartRepository.findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        CartItemModel item = cartItemRepository.findByCart_CartIDAndCartItemID(cart.getCartID(), requestEditCartItemDTO.getCartItemId())
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        item.setAmount(requestEditCartItemDTO.getNewAmount());

        cartItemRepository.save(item);
        return new ResponseEditCartItemDTO(
                requestEditCartItemDTO.getCartItemId(),
                requestEditCartItemDTO.getNewAmount(),
                item.getType().getPrice()
        );
    }

    public BigDecimal calculateTotalPrice(String passengerId) {
        CartDto cartDto = getOrCreateCart(passengerId);
        return cartDto.getTotalPrice();
    }


    private CartModel findCart(String passengerId) {
        return cartRepository.findByPassengerModel_PassengerID(passengerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for passenger"));
    }

    private TicketModel findTicketType(pawa_be.ticket.external.enumerator.TicketType ticketType) {
        return ticketTypeRepository.findById(ticketType)
                .orElseThrow(() -> new NotFoundException("Ticket type not found: " + ticketType));
    }

    private CartModel createNewCart(String passengerId) {
        CartModel newCart = new CartModel();
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);
        newCart.setPassengerModel(passenger);
        return cartRepository.save(newCart);
    }

    private CartDto toCartDto(CartModel cart) {
        List<CartItemModel> items = cartItemRepository.findByCart(cart);

        LocalDateTime now = LocalDateTime.now();
        List<CartItemModel> expiredItems = items.stream()
                .filter(item -> item.getCreatedAt().plus(CART_EXPIRY).isBefore(now))
                .toList();

        if (!expiredItems.isEmpty()) {
            cartItemRepository.deleteAll(expiredItems);
            items.removeAll(expiredItems);
        }

        List<CartItemDto> itemDtos = items.stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemDtos.stream()
                .map(dto -> dto.getPrice().multiply(BigDecimal.valueOf(dto.getAmount())))
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
                .amount(item.getAmount())
                // Station names would ideally come from an integration with a metro line
                // service
                .startStationId(item.getStartStationID())
                .endStationId(item.getEndStationID())
                .ticketType(item.getType().getTicketType())
                .ticketTypeName(item.getType().getDisplayName())
                .price(item.getType().getPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .displayName(item.getType().getDisplayName())
                .duration(item.getType().getExpiryDescription())
                .build();
    }
}
