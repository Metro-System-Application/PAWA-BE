package pawa_be.insfrastructure.stripe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestPaymentDataDTO {
    @NotNull
    private final String userId;

    @NotNull
    private final String userEmail;

    @NotNull
    private final Long price;
}
