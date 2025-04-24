package pawa_be.ticket.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.repository.TicketRepository;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class ExternalTicketService implements IExternalTicketService {
    @Autowired
    TicketRepository ticketRepository;

    @Override
    public BigDecimal getTicketPriceByEnum(TicketType ticketType) {
        // TODO: use this when ticket is ready
//        return ticketRepository.findByTicketType(ticketType).orElseThrow(
//                () -> new NotFoundException(String.format("TICKET_TYPE '%s' not found", ticketType))
//        ).getPrice();
        return ticketType.getPrice();
    }
}
