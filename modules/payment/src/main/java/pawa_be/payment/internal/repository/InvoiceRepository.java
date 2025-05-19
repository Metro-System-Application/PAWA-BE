package pawa_be.payment.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.model.InvoiceModel;
import pawa_be.profile.internal.model.PassengerModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends CrudRepository<InvoiceModel, UUID> {
    List<InvoiceModel> findByPassengerModel_PassengerID(String passengerId);

    Optional<InvoiceModel> findByInvoiceId(UUID invoiceId);

    List<InvoiceModel> findByEmail(String email);
}
