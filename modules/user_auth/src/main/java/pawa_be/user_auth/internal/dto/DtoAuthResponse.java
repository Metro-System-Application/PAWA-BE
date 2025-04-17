package pawa_be.user_auth.internal.dto;

import lombok.Getter;

@Getter
public class DtoAuthResponse {
    private String token;

    protected DtoAuthResponse() {}

    public DtoAuthResponse(String token) {
        this.token = token;
    }

}
