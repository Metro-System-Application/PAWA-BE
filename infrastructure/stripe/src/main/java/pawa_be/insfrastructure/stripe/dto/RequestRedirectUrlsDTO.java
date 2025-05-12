package pawa_be.insfrastructure.stripe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestRedirectUrlsDTO {
    @NotNull
    private final String successUrl;

    @NotNull
    private final String cancelUrl;
}
