package pawa_be.insfrastructure.stripe.service;

import com.stripe.exception.StripeException;
import pawa_be.insfrastructure.stripe.dto.*;

import java.util.List;

public interface IStripeService {
    ResponseCreateStripeSessionDTO createTopUpPaymentSession(RequestPaymentDataDTO userData, long price, RequestRedirectUrlsDTO redirectData) throws StripeException;
    ResponseCreateStripeSessionDTO createDirectTicketPaymentSession(RequestPaymentDataDTO userData, RequestRedirectUrlsDTO redirectData, List<LineItemRequestDTO> items) throws StripeException;
    ResponseProcessSuccessfulTopUpDTO processSuccessfulTopUp(String payload) throws StripeException;
}
