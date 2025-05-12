package pawa_be.ticket.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that provides ticket-related operations
 * This service now uses the database-driven approach instead of enum constants
 */
@Service
public class TicketService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    /**
     * Get all active ticket types from the database
     */
    public List<TypeDto> getAllTicketType() {
        return ticketTypeRepository.findByActiveTrue()
                .stream()
                .map(ticketModel -> new TypeDto(
                        ticketModel.getTicketType(),
                        ticketModel.getDisplayName(),
                        ticketModel.getPrice(),
                        ticketModel.getExpiryDescription(),
                        ticketModel.getEligibilityRequirements(),
                        ticketModel.getExpiryInterval(),
                        ticketModel.getActive()))
                .collect(Collectors.toList());
    }
}
