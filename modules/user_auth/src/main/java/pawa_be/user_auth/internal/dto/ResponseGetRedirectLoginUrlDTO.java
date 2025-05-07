package pawa_be.user_auth.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseGetRedirectLoginUrlDTO {
    String redirectUrl;
}
