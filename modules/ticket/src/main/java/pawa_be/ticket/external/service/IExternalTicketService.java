package pawa_be.ticket.external.service;

import org.apache.commons.lang3.tuple.Pair;
import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;
import java.util.List;

public interface IExternalTicketService {
    BigDecimal getTicketPriceByEnum(TicketType ticketType);
    Pair<Boolean, String> checkEligibleTicketType(TicketType ticketType, String passengerId);
}
