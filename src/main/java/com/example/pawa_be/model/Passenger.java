package com.example.pawa_be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="passenger")
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
