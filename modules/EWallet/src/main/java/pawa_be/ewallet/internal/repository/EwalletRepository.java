package pawa_be.ewallet.internal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pawa_be.ewallet.internal.model.EwalletModel;

@Repository
public interface EwalletRepository extends CrudRepository<EwalletModel, String> {

}
