package pawa_be.payment.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemForInvoiceDTO {
    @NotEmpty(message = "Ticket name cannot be empty")
    private String ticketName;

    @NotEmpty(message = "Ticket type cannot be empty")
    private String ticketType;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Line ID cannot be null")
    private UUID lineID;

    @NotEmpty(message = "Line name cannot be empty")
    private String lineName;

    private String startStation;

    private String endStation;

    @Min(value = 1, message = "Duration must be at least 1")
    private int duration;
}
