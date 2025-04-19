package pawa_be.payment.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.payment.internal.model.EwalletModel;

@Repository
public interface PaymentRepository extends CrudRepository<EwalletModel, String> {

}
