package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pawa_be.profile.internal.enumeration.ImageType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="images")
@Data
public class ImagesModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID imageID;

    @Column(nullable = false)
    private String imageURL;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @OneToOne
    @JoinColumn(name = "passenger_id", referencedColumnName = "passengerID", nullable = false, updatable = false)
    private PassengerModel passengerModel;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
