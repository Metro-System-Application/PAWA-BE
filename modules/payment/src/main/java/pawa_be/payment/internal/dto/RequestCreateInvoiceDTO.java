package pawa_be.payment.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pawa_be.payment.internal.model.InvoiceItemModel;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestCreateInvoiceDTO {
    private String passengerId;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Cart items cannot be null")
    private List<CartItemForInvoiceDTO> cartItems;
}
