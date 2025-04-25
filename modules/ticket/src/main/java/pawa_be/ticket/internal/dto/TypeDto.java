package pawa_be.ticket.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;
import java.time.Duration;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeDto {
    @Schema
    private TicketType ticketType;

    @Schema
    private String typeName;

    @Schema
    private BigDecimal price;

    @Schema
    private String expiryDescription;

    @Schema
    private String requirementDescription;

    @Schema
    private Duration expiryInterval;
}
