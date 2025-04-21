package pawa_be.user_auth.internal.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.user_auth.internal.model.UserAuthModel;

@Getter
@AllArgsConstructor
public class UpdateUserResult {
    private final UpdateUserAuthDataResult status;
    private final UserAuthModel user;
}
