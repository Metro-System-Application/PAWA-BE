package com.example.pawa_be.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "user_auth")

public class UserAuth {
    @Id
    private String password;
    private String email;
}
