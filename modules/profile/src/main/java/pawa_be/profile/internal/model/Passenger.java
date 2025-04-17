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
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String passengerID;

    @Column(unique = true)
    private String passengerEmail;

    //User name
    private String passengerFirstName;
    private String passengerMiddleName;
    private String passengerLastName;

    //User contact
    private String passengerPhone;
    private String passengerAddress;

//  This json serialization is only for modifying the date format to DD/MM/YYYY if needed
//    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate passengerDateOfBirth;

    //User check for specific conditions
    private Boolean hasDisability;
    private Boolean isRevolutionary;

    //User verification
    private String nationalID;
    private String studentID;
    private String googleID;

    //Account status
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
