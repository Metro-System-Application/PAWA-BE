package pawa_be.insfrastructure.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseCreateStripeSessionDTO {
    private final String redirectUrl;
}
