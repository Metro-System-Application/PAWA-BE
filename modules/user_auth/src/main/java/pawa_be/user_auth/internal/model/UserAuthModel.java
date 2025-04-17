package pawa_be.user_auth.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.Getter;

@Getter
@Entity
@Table(name = "user_auth")
public class UserAuthModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long userId;

    @Column(unique = true)
    private String email;

    private String password;
    private String role;

    public UserAuthModel(String username, String password, String role) {
        this.email = username;
        this.password = password;
        this.role = role;
    }

    public UserAuthModel() {}
}
