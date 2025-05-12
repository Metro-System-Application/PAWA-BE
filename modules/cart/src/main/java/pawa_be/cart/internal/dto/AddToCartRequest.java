package pawa_be.cart.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pawa_be.ticket.external.enumerator.TicketType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull
    @Schema(description = "The ID of the metro line", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID lineId;

    @NotNull
    @Schema(description = "The ID of the starting station", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID startStationId;

    @NotNull
    @Schema(description = "The ID of the destination station", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID endStationId;

    @NotNull
    @Schema(description = "The type of ticket", example = "ONE_WAY_4")
    private TicketType ticketType;
}
