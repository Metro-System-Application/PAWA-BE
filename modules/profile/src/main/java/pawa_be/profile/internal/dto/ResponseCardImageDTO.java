package pawa_be.profile.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ResponseCardImageDTO {
    private List<ImageDTO> studentIdPictures;
    private List<ImageDTO> nationalIdPictures;
}
