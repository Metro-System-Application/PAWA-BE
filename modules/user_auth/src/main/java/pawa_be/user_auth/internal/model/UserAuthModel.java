package pawa_be.user_auth.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;

@Getter
@Setter
@Entity
@Table(name = "user_auth")
@NoArgsConstructor
public class UserAuthModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(unique = true)
    private String email;

    private String password;
    // TODO: add again when connect the db with OPWA
    @Enumerated(EnumType.STRING)
    private UserRoleConfig role;

    public UserAuthModel(String username, String password) {
        this.email = username;
        this.password = password;
        // TODO: add again when connect the db with OPWA
        this.role = UserRoleConfig.PASSENGER;
    }
}
