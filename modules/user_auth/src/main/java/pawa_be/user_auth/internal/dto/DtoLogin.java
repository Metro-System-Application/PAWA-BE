package pawa_be.user_auth.internal.dto;

import lombok.Getter;

@Getter
public class DtoLogin {
    protected String email;
    protected String password;

    protected DtoLogin() {}

    protected DtoLogin(String username, String password) {
        this.email = username;
        this.password = password;
    }

}
