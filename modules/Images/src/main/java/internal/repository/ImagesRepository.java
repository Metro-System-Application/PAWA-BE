package internal.repository;

import internal.model.ImagesModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImagesRepository extends CrudRepository<ImagesModel, UUID> {

}
