package pawa_be.ticket.external.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    @JsonCreator
    public static AuthResponse fromToken(String plainToken) {
        AuthResponse response = new AuthResponse();
        response.setToken(plainToken);
        return response;
    }
}
