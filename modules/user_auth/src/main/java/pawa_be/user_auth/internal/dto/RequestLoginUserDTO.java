package pawa_be.user_auth.internal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestLoginUserDTO {
    @NotNull
    protected String email;

    @NotNull
    protected String password;
}
