package pawa_be.insfrastructure.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseProcessSuccessfulTopUpDTO {
    String userid;
    String userEmail;
    Long amount;
}
