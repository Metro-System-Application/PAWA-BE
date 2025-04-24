package pawa_be.ticket.external.service;

import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;

public interface IExternalTicketService {
    BigDecimal getTicketPriceByEnum(TicketType ticketType);
}
