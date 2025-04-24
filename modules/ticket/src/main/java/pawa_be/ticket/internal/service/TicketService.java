package pawa_be.ticket.internal.service;

import org.springframework.stereotype.Service;
import pawa_be.ticket.internal.Enumerator.TicketType;
import pawa_be.ticket.internal.dto.TypeDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    public List<TypeDto> getAllTicketType(){
        return Arrays.stream(TicketType.values())
                .map(this::convertTicketTypeDto)
                .collect(Collectors.toList());
    }

    private TypeDto convertTicketTypeDto(TicketType ticketType) {
        return new TypeDto(
                ticketType,
                ticketType.getDisplayedName(),
                ticketType.getPrice(),
                ticketType.getExpiryDescription(),
                ticketType.getEligibilityRequirements()
        );
    }
}
