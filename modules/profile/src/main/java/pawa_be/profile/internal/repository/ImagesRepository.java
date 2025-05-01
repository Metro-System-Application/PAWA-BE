package pawa_be.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pawa_be.profile.internal.enumeration.ImageType;
import pawa_be.profile.internal.model.ImagesModel;

import java.util.Optional;
import java.util.UUID;

public interface ImagesRepository extends JpaRepository<ImagesModel, UUID> {
    Optional<ImagesModel> findByPassengerModel_PassengerIDAndImageType(String passengerId, ImageType imageType);
}