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
    public UserAuth register(@RequestBody UserAuth user) throws Exception {
        return userAuthService.registerNewUser(user);
    }

    @PostMapping("/login")
    public String loginWithEmail(@RequestBody UserAuth userAuthInstance){
        return userAuthService.loginUserWithEmail(userAuthInstance);
    }


}
