package pawa_be.profile.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.profile.internal.enumeration.ImageType;

@AllArgsConstructor
@Getter
public class ImageMetadataDTO {
    private String key;
    private String mimeType;
    private ImageType imageType;
}
