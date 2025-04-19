package pawa_be.user_auth.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestLoginUserDTO {
    protected String email;
    protected String password;
}
