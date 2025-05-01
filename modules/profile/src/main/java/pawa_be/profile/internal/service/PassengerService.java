package pawa_be.profile.internal.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pawa_be.bucket.service.BucketService;
import pawa_be.profile.internal.dto.*;
import pawa_be.profile.internal.enumeration.ImageType;
import pawa_be.profile.internal.model.ImagesModel;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.ImagesRepository;
import pawa_be.profile.internal.repository.PassengerRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerService {
    @Autowired
    private final PassengerRepository passengerRepository;

    @Autowired
    private final ImagesRepository imagesRepository;

    @Autowired
    private final BucketService bucketService;

    public void updateCurrentPassengerById(String passengerId, @Valid RequestUpdatePassengerDTO updatedInfo) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);

        if (updatedInfo.getPassengerPhone() != null) {
            passenger.setPassengerPhone(updatedInfo.getPassengerPhone());
        }

        if (updatedInfo.getPassengerAddress() != null) {
            passenger.setPassengerAddress(updatedInfo.getPassengerAddress());
        }

        passenger.setUpdatedAt(LocalDateTime.now());
        passengerRepository.save(passenger);
    }

    public ResponsePassengerDTO getCurrentPassengerById(String passengerId) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);

        return new ResponsePassengerDTO(
                passenger.getPassengerFirstName(),
                passenger.getPassengerMiddleName(),
                passenger.getPassengerLastName(),
                passenger.getPassengerPhone(),
                passenger.getPassengerAddress(),
                passenger.getPassengerDateOfBirth(),
                passenger.getNationalID(),
                passenger.getStudentID(),
                passenger.getHasDisability(),
                passenger.getIsRevolutionary()
        );
    }

    public void uploadOrUpdatePassengerImage(String passengerId, @Valid RequestUploadImageDTO requestUploadImageDTO) {
        MultipartFile file = requestUploadImageDTO.getFile();

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/jpeg") &&
                        !contentType.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG image formats are allowed.");
        }

        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerID(passengerId);
        String fileKey = UUID.randomUUID().toString();

        ImagesModel existingImage = imagesRepository
                .findByPassengerModel_PassengerIDAndImageType(passengerId, requestUploadImageDTO.getImageType())
                .orElse(null);

        if (existingImage != null) {
            bucketService.removeFile(existingImage.getImageURL());
        }

        bucketService.uploadFile(fileKey, file);
        ImagesModel image = existingImage != null ? existingImage : new ImagesModel();

        image.setImageType(requestUploadImageDTO.getImageType());
        image.setImageURL(fileKey);
        image.setMimeType(contentType);
        image.setPassengerModel(passenger);

        imagesRepository.save(image);
    }

    private ImageMetadataDTO getImageData(String passengerId, ImageType imageType) {
        return imagesRepository
                .findByPassengerModel_PassengerIDAndImageType(passengerId, imageType)
                .map(image -> new ImageMetadataDTO(image.getImageURL(), image.getMimeType(), image.getImageType()))
                .orElse(null);
    }

    public ResponseProfileImageDTO getProfileImage(String passengerId) {
        ImageMetadataDTO profileImage = getImageData(passengerId, ImageType.USER_PROFILE);
        if (profileImage == null) {
            return new ResponseProfileImageDTO(null);
        }

        String profileImageBase64 = bucketService.getBase64File(profileImage.getKey());
        return new ResponseProfileImageDTO(
                new ImageDTO(profileImageBase64, profileImage.getMimeType(), profileImage.getImageType())
        );
    }


    public ResponseCardImageDTO getCardImagesBase64(String passengerId) {
        List<ImageDTO> studentIdImages = new ArrayList<>();
        List<ImageDTO> nationalIdImages = new ArrayList<>();

        ImageMetadataDTO studentIdFrontMetadata = getImageData(passengerId, ImageType.STUDENT_ID_FRONT);
        if (studentIdFrontMetadata != null) {
            String base64 = bucketService.getBase64File(studentIdFrontMetadata.getKey());
            if (base64 != null) {
                studentIdImages.add(new ImageDTO(base64, studentIdFrontMetadata.getMimeType(), studentIdFrontMetadata.getImageType()));
            }
        }

        ImageMetadataDTO studentIdBackMetadata = getImageData(passengerId, ImageType.STUDENT_ID_BACK);
        if (studentIdBackMetadata != null) {
            String base64 = bucketService.getBase64File(studentIdBackMetadata.getKey());
            if (base64 != null) {
                studentIdImages.add(new ImageDTO(base64, studentIdBackMetadata.getMimeType(), studentIdBackMetadata.getImageType()));
            }
        }

        ImageMetadataDTO nationalIdFrontMetadata = getImageData(passengerId, ImageType.NATIONAL_ID_FRONT);
        if (nationalIdFrontMetadata != null) {
            String base64 = bucketService.getBase64File(nationalIdFrontMetadata.getKey());
            if (base64 != null) {
                nationalIdImages.add(new ImageDTO(base64, nationalIdFrontMetadata.getMimeType(), nationalIdFrontMetadata.getImageType()));
            }
        }

        ImageMetadataDTO nationalIdBackMetadata = getImageData(passengerId, ImageType.NATIONAL_ID_BACK);
        if (nationalIdBackMetadata != null) {
            String base64 = bucketService.getBase64File(nationalIdBackMetadata.getKey());
            if (base64 != null) {
                nationalIdImages.add(new ImageDTO(base64, nationalIdBackMetadata.getMimeType(), nationalIdBackMetadata.getImageType()));
            }
        }

        return new ResponseCardImageDTO(
                nationalIdImages.isEmpty() ? null : nationalIdImages,
                studentIdImages.isEmpty() ? null : studentIdImages
        );
    }
}
