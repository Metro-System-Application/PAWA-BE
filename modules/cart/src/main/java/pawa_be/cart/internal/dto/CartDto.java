package pawa_be.cart.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private UUID cartId;
    private String passengerId;
    private List<CartItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Price calculation - not stored in the database but calculated on retrieval
    private BigDecimal totalPrice;
}
