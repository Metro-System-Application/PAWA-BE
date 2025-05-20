package pawa_be.ticket.internal.service;

import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.model.TicketModel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface ITicketTypeService {
    List<TypeDto> getAllTicketTypes(String metroLineId);
    List<TypeDto> getGuestTicketTypes(String metroLineId);
    List<TypeDto> getStudentTicketTypes(String metroLineId);
    TypeDto getBestTicketByMetroLine(String metroLineId);
    List<TypeDto> getBestTicketsForPassengerWithMetroLine(String email, String metroLineId);
}
