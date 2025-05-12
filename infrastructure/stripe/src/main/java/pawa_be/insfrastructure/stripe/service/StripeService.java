package pawa_be.insfrastructure.stripe.service;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pawa_be.insfrastructure.stripe.dto.*;

import java.util.List;

@Service
public class StripeService implements IStripeService {
    @Value("${stripe.secret_key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    private SessionCreateParams.LineItem buildLineItem(String name, long amountInVND, long quantity) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(quantity)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("vnd")
                                .setUnitAmount(amountInVND)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(name)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public ResponseCreateStripeSessionDTO createTopUpPaymentSession(RequestPaymentDataDTO userData, long price, RequestRedirectUrlsDTO redirectData) throws StripeException {
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(redirectData.getSuccessUrl())
                .setCancelUrl(redirectData.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("vnd")
                                                .setUnitAmount(price)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Wallet Top-Up")
                                                                .build())
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setSetupFutureUsage(SessionCreateParams.PaymentIntentData.SetupFutureUsage.OFF_SESSION)
                                .build()
                )
                .setClientReferenceId(userData.getUserId())
                .setCustomerEmail(userData.getUserEmail());

        Session session = Session.create(sessionBuilder.build());
        return new ResponseCreateStripeSessionDTO(session.getUrl());
    }

    public ResponseCreateStripeSessionDTO createDirectTicketPaymentSession(
            RequestPaymentDataDTO userData,
            RequestRedirectUrlsDTO redirectData,
            List<LineItemRequestDTO> items
    ) throws StripeException {
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(redirectData.getSuccessUrl())
                .setCancelUrl(redirectData.getCancelUrl())
                .setClientReferenceId(userData.getUserId())
                .setCustomerEmail(userData.getUserEmail())
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setSetupFutureUsage(SessionCreateParams.PaymentIntentData.SetupFutureUsage.OFF_SESSION)
                                .build()
                );

        for (LineItemRequestDTO item : items) {
            SessionCreateParams.LineItem lineItem = buildLineItem(item.getName(), item.getAmountInVND(), item.getQuantity());
            sessionBuilder.addLineItem(lineItem);
        }

        Session session = Session.create(sessionBuilder.build());
        return new ResponseCreateStripeSessionDTO(session.getUrl());
    }

    public ResponseProcessSuccessfulTopUpDTO processSuccessfulTopUp(String payload) throws StripeException {
        Event event;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid Stripe payload", e);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            throw new RuntimeException("Unable to deserialize Stripe object from event payload");
        }

        if (!(stripeObject instanceof Session)) {
            throw new RuntimeException("Expected Session object but received: " + stripeObject.getClass().getSimpleName());
        }

        Session session = (Session) stripeObject;

        if (!"complete".equals(session.getStatus())) {
            throw new IllegalStateException("Payment session not completed");
        }

        String userId = session.getClientReferenceId();
        String userEmail = session.getCustomerEmail();
        Long amount = session.getAmountTotal();

        return new ResponseProcessSuccessfulTopUpDTO(userId, userEmail, amount);
    }

}
