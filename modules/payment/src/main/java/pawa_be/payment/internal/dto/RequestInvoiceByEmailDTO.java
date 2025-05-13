package pawa_be.payment.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInvoiceByEmailDTO {
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    private String email;
}
