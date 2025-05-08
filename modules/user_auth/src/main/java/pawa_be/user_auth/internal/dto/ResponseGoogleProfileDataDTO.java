package pawa_be.user_auth.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseGoogleProfileDataDTO {
    private final String firstName;
    private final String lastName;
}
