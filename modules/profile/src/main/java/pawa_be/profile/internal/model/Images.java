package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.profile.internal.enumerator.ImageType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="Passenger")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID imageID;

    //FK, modify later
    private UUID passengerID;

    private String imageURL;
    private ImageType imageType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
