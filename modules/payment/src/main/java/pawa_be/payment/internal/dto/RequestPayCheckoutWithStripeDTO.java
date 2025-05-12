package pawa_be.payment.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPayCheckoutWithStripeDTO {
    @NotBlank
    String successUrl;
    @NotBlank
    String cancelUrl;
}