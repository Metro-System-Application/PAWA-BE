package pawa_be.payment.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.model.EwalletModel;
import pawa_be.payment.internal.model.TopUpTransactionModel;

@Repository
public interface TopUpTransactionRepository extends CrudRepository<TopUpTransactionModel, String> {

}
