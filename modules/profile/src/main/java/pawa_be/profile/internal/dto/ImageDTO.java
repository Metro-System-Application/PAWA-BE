package pawa_be.profile.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.profile.internal.enumeration.ImageType;

@AllArgsConstructor
@Getter
public class ImageDTO {
    private String base64;
    private String mimeType;
    private ImageType imageType;
}
