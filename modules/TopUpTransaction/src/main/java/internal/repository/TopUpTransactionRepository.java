package internal.repository;

import internal.model.TopUpTransactionModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TopUpTransactionRepository extends CrudRepository<TopUpTransactionModel, UUID> {

}
