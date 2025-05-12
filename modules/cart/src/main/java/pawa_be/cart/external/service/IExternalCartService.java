package pawa_be.cart.external.service;

import pawa_be.cart.dto.ResponseGetCartContentsDTO;

public interface IExternalCartService {
    ResponseGetCartContentsDTO getCartContents(String passengerId);
    void cleanCart(String passengerId);
}
