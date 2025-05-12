package pawa_be.profile.internal.model;

//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="passenger")
@Data
public class PassengerModel {
    @Id
    private String passengerID;

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
    private String passengerEmail;
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
    @CreationTimestamp
    @Column(updatable = false,nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
