package pawa_be.ticket.internal.service;


import pawa_be.ticket.internal.dto.TypeDto;

import java.util.List;

public interface ITicketService {
    public List<TypeDto> getAllTicketType();
}
