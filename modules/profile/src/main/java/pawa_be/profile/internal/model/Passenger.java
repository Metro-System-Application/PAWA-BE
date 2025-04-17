package pawa_be.profile.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="Passenger")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID passengerID;

    //Passenger name
    private String passengerFirstName;
    private String passengerMiddleName;
    private String passengerLastName;

    //Passenger email
    @Column(unique = true)
    private String passengerEmail;

    //Passenger contact
    private String passengerPhone;
    private String passengerAddress;

    //Passenger background
    private LocalDate passengerDateOfBirth;
    private Boolean passengerHasDisability;
    private Boolean isRevolutionary;

    //Passenger PII
    private String nationalID;
    private String googleID;
    private String studentID;

    //Passenger account timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
