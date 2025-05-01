package pawa_be.ticket.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;

@Service
class ExternalTicketService implements IExternalTicketService {
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Override
    public BigDecimal getTicketPriceByEnum(TicketType ticketType) {
        return ticketTypeRepository.findById(ticketType)
                .orElseThrow(() -> new NotFoundException(String.format("TICKET_TYPE '%s' not found", ticketType)))
                .getPrice();
    }
}
