package com.example.pawa_be.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class PassengerController {

    @GetMapping("")
    public String greet(){
        return "Hello, Passenger!";
    }
}
