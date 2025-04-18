package internal.repository;

import internal.model.InvoiceItemModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvoiceItemRepository extends CrudRepository<InvoiceItemModel, UUID> {

}
