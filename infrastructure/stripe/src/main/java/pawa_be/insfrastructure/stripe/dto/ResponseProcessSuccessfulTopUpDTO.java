package pawa_be.insfrastructure.stripe.dto;

import com.stripe.model.LineItemCollection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseProcessSuccessfulTopUpDTO {
    String userid;
    String userEmail;
    Long amount;
    private String transactionId;
    private LineItemCollection lineItems;
}
