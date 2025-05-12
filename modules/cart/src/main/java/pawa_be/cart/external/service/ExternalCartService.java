package pawa_be.cart.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.cart.external.dto.CartContentDTO;
import pawa_be.cart.external.dto.ResponseGetCartContentsDTO;
import pawa_be.cart.internal.model.CartItemModel;
import pawa_be.cart.internal.repository.CartItemRepository;
import pawa_be.cart.internal.service.CartService;

import java.util.List;

@Service
public class ExternalCartService implements IExternalCartService {
    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    CartService cartService;

    public ResponseGetCartContentsDTO getCartContents(String passengerId) {
        List<CartItemModel> items = cartItemRepository.findAllByCart_PassengerModel_PassengerID(passengerId);
        List<CartContentDTO> contents = items.stream()
                .map(item -> new CartContentDTO(
                        item.getType().getDisplayName(),
                        item.getType().getPrice(),
                        item.getAmount()
                ))
                .toList();

        return new ResponseGetCartContentsDTO(contents);
    }

    public void cleanCart(String passengerId) {
        cartService.clearCart(passengerId);
    }
}
