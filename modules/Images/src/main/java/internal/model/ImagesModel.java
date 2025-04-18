package internal.model;

import internal.Enumerator.ImageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawa_be.profile.internal.model.Passenger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="images")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImagesModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID imageID;

    private String imageURL;
    private ImageType imageType;

    @OneToOne
    @JoinColumn(name = "passenger_id", referencedColumnName = "passengerID", nullable = false)
    private Passenger passenger;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
