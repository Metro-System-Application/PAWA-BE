package pawa_be.user_auth.internal.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;

@Entity
@Table(name = "user_auth")
@NoArgsConstructor
@Data
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
