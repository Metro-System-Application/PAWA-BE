package pawa_be.payment.internal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.enumeration.TicketStatus;
import pawa_be.payment.internal.model.InvoiceItemModel;
import pawa_be.payment.internal.model.InvoiceModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItemModel, UUID> {
    List<InvoiceItemModel> findByInvoiceModel(InvoiceModel invoiceModel);

    List<InvoiceItemModel> findByInvoiceModel_InvoiceId(UUID invoiceId);
    
    @Query("SELECT item FROM InvoiceItemModel item WHERE item.invoiceModel.passengerModel.passengerID = :passengerId")
    Page<InvoiceItemModel> findAllByPassengerId(@Param("passengerId") String passengerId, Pageable pageable);

    Page<InvoiceItemModel> findAllByInvoiceModel_PassengerModel_PassengerIDAndStatus(
            String passengerId,
            TicketStatus status,
            Pageable pageable);
}
