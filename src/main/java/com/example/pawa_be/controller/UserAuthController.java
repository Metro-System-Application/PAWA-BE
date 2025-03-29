package com.example.pawa_be.controller;

import com.example.pawa_be.model.UserAuth;
import com.example.pawa_be.repository.UserAuthRepository;
import com.example.pawa_be.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("")
public class UserAuthController {
    @Autowired
    private UserAuthService userAuthService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String checkRegister(){
        return("In register");
    }

    @PostMapping("/register")
    public UserAuth registerNewUser(@RequestBody UserAuth user) throws Exception {
        if (Objects.isNull(userAuthService.findUserAuthByEmail(user.getEmail()))){
            return userAuthService.registerUser(user);
        }
        throw new Exception("User already exist!");
    }

    @PostMapping("/login")
    public String loginWithEmail(@RequestBody UserAuth userAuthInstance){
        if (userAuthService.validatePassword(userAuthInstance.getEmail(), userAuthInstance.getPassword())) {
            return "Success!";
        } else {
            return "Invalid credentials!";
        }
    }


}
