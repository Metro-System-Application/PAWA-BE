package pawa_be.user_auth.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseGoogleAuthResultDTO {
    private final boolean profileComplete;
    private final String authToken;
    private final String tempToken;
    private final ResponseGoogleProfileDataDTO profileData;
}
