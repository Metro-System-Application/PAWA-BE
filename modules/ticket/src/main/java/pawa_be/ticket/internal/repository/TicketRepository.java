package pawa_be.ticket.internal.repository;

import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.model.TicketModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends CrudRepository<TicketModel, UUID> {
    Optional<TicketModel> findByTicketType(TicketType ticketType);
}
