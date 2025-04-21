package pawa_be.profile.internal.model;

//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="passenger")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PassengerModel {
    @Id
    private String passengerID;

    // TODO: to be removed
    @Column(unique = true)
    private String passengerEmail;

    //User name
    @Column(nullable = false)
    private String passengerFirstName;
    @Column(nullable = false)
    private String passengerMiddleName;
    @Column(nullable = false)
    private String passengerLastName;

    //User contact
    @Column(nullable = false)
    private String passengerPhone;
    @Column(nullable = false)
    private String passengerAddress;

//  This json serialization is only for modifying the date format to DD/MM/YYYY if needed
//    @JsonSerialize(using = LocalDateSerializer.class)
    @Column(nullable = false)
    private LocalDate passengerDateOfBirth;

    //User check for specific conditions
    @Column(nullable = false)
    private Boolean hasDisability;
    @Column(nullable = false)
    private Boolean isRevolutionary;

    //User verification
    private String nationalID;
    private String studentID;
    private String googleID;

    //Account status
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
