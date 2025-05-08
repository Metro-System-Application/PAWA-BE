package pawa_be.user_auth.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(description = "Basic profile data received from Google")
public class ResponseGoogleProfileDataDTO {

    @Schema(description = "Given name of the user", example = "John")
    private String givenName;

    @Schema(description = "Family name of the user", example = "Doe")
    private String familyName;

}
