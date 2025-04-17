package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "UserAuth")
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userID;

    private String userAuthEmail;
    private String userAuthPassword;
}
