package pawa_be.payment.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.model.InvoiceItemModel;
import pawa_be.payment.internal.model.InvoiceModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceItemRepository extends CrudRepository<InvoiceItemModel, UUID> {
    List<InvoiceItemModel> findByInvoiceModel(InvoiceModel invoiceModel);

    List<InvoiceItemModel> findByInvoiceModel_InvoiceID(UUID invoiceId);
}
