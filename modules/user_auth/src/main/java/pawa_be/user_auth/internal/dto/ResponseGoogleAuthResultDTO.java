package pawa_be.user_auth.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Result of Google authentication process")
public class ResponseGoogleAuthResultDTO {

    @Schema(description = "Whether the user's profile is fully completed", example = "false")
    private boolean profileComplete;

    @Schema(description = "Authentication token for the user", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String authToken;

    @Schema(description = "Temporary token if the profile is incomplete", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String tempToken;

    @Schema(description = "Basic profile data from Google, needed to complete registration")
    private ResponseGoogleProfileDataDTO profileData;
}
