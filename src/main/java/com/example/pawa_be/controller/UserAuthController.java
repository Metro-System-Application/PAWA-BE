package com.example.pawa_be.controller;

import com.example.pawa_be.model.UserAuth;
import com.example.pawa_be.repository.UserAuthRepository;
import com.example.pawa_be.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class UserAuthController {
    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("")
    public String checkRegister(){
        return("In register");
    }

    @PostMapping("")
    public UserAuth registerNewUser(@RequestBody UserAuth user){
        System.out.println("This is" + user.toString());
        return userAuthService.registerUser(user);
    }
}
