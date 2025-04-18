package internal.repository;

import internal.model.TypeModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TypeRepository extends CrudRepository<TypeModel, UUID> {

}
