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
    @Enumerated(EnumType.STRING)
    private UserRoleConfig role;

    private String googleId;

    public static UserAuthModel fromPassword(String username, String password) {
        return new UserAuthModel(username, password, true);
    }

    public static UserAuthModel fromGoogleId(String username, String googleId) {
        return new UserAuthModel(username, googleId, false);
    }

    private UserAuthModel(String username, String credential, boolean isPassword) {
        this.email = username;
        if (isPassword) {
            this.password = credential;
        } else {
            this.googleId = credential;
        }
        this.role = UserRoleConfig.PASSENGER;
    }
}
