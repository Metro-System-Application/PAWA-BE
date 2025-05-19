package pawa_be.ticket.external.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.profile.external.service.IExternalPassengerService;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;

@Service
class ExternalTicketService implements IExternalTicketService {
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private IExternalPassengerService passengerService;

    @Override
    public BigDecimal getTicketPriceByEnum(TicketType ticketType) {
        return ticketTypeRepository.findById(ticketType)
                .orElseThrow(() -> new NotFoundException(String.format("TICKET_TYPE '%s' not found", ticketType)))
                .getPrice();
    }

    @Override
    public Pair<Boolean, String> checkEligibleTicketType(TicketType ticketType, String passengerId) {
        if (ticketType == TicketType.FREE) {
            if (passengerService.isRevolutionary(passengerId)
                    || passengerService.hasDisabilities(passengerId)
                    || passengerService.isBelow6orAbove60YearsOld(passengerId)) {
                return Pair.of(true, "");
            } else {
                return Pair.of(false, "Passenger is not eligible for free ticket");
            }
        }

        if (ticketType == TicketType.MONTHLY_STUDENT) {
            if (passengerService.isStudent(passengerId)) {
                return Pair.of(true, "");
            } else {
                return Pair.of(false, "Passenger is not eligible for monthly student ticket");
            }
        }

        return Pair.of(true, "");
    }

    public int getTicketDuration(String ticketType) {
        TicketType mappedTicketType = TicketType.fromString(ticketType);
        TicketModel ticket = ticketTypeRepository.findByTicketType(mappedTicketType)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        return (int) ticket.getExpiryInterval().toDays();
    }
}
