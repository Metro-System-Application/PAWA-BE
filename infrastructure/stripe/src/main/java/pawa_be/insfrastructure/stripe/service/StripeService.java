package pawa_be.insfrastructure.stripe.service;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.LineItemCollection;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListLineItemsParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pawa_be.insfrastructure.stripe.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeService implements IStripeService {
    @Value("${stripe.secret_key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    private SessionCreateParams.LineItem buildLineItem(LineItemRequestDTO item) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ticket_type", item.getTicketType());
        metadata.put("line_id", item.getLineId());
        metadata.put("line_name", item.getLineName());
        metadata.put("start_station", item.getStartStation());
        metadata.put("end_station", item.getEndStation());

        return SessionCreateParams.LineItem.builder()
                .setQuantity(item.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("vnd")
                                .setUnitAmount(item.getAmountInVND())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getName())
                                                .putAllMetadata(metadata)
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

        for (LineItemRequestDTO item: items) {
            sessionBuilder.addLineItem(buildLineItem(item));
        }

        Session session = Session.create(sessionBuilder.build());
        return new ResponseCreateStripeSessionDTO(session.getUrl());
    }

    public ResponseProcessSuccessfulTopUpDTO processSuccessfulTransaction(String payload) throws StripeException {
        Event event;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid Stripe payload", e);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject()
                .orElseThrow(() -> new RuntimeException("Unable to deserialize Stripe object from event payload"));

        if (!(stripeObject instanceof Session)) {
            throw new RuntimeException("Expected Session object but received: " + stripeObject.getClass().getSimpleName());
        }

        Session session = (Session) stripeObject;

        if (!"complete".equals(session.getStatus())) {
            throw new IllegalStateException("Payment session not completed");
        }

        SessionListLineItemsParams params = SessionListLineItemsParams.builder().addExpand("data.price.product").build();
        LineItemCollection lineItems = session.listLineItems(params);

        String paymentIntentId = session.getPaymentIntent();

        String userId = session.getClientReferenceId();
        String userEmail = session.getCustomerEmail();
        Long amount = session.getAmountTotal();

        return new ResponseProcessSuccessfulTopUpDTO(userId, userEmail, amount, paymentIntentId, lineItems);
    }
}
