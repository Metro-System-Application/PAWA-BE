package pawa_be.profile.internal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import pawa_be.profile.internal.enumeration.ImageType;

import java.util.UUID;

@Getter
@Setter
public class RequestUploadImageDTO {

    @NotNull
    private UUID passengerId;

    @NotNull
    private ImageType imageType;

    @NotNull
    private MultipartFile file;

    public RequestUploadImageDTO(ImageType imageType, MultipartFile file) {
        this.imageType = imageType;
        this.file = file;
    }
}
