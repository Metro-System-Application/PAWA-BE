package pawa_be.user_auth.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;

@Getter
@AllArgsConstructor
public class ResponseRegisterUserDTO {
    @Schema(example = "101")
    private String userId;

    @Schema(example = "user@example.com")
    private String email;

    @Schema(example = "PASSENGER")
    private UserRoleConfig role;
}

