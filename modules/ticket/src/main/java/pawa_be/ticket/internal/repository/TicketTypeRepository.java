package pawa_be.ticket.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.model.TicketModel;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketModel, TicketType> {
    List<TicketModel> findByActiveTrue();
}
