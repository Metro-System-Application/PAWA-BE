package pawa_be.infrastructure.jwt.config;

import lombok.Getter;

@Getter
public enum UserRoleConfig {
    PASSENGER("PASSENGER"),
    ADMIN("ADMIN"),
    GUEST("GUEST");

    private final String roleName;

    UserRoleConfig(String roleName) {
        this.roleName = roleName;
    }

}

