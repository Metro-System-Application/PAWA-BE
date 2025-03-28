package com.example.pawa_be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name="passenger")
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    @Id
    private String passengerFirstName;
    private String passengerMiddleName;
    private String passengerLastName;

    @Column(unique = true)
    private String passengerEmail;
    private String passengerPhone;
    private String passengerAddress;


}
