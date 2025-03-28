package com.example.pawa_be.service;


import com.example.pawa_be.model.UserAuth;
import com.example.pawa_be.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserAuth registerUser(UserAuth userAuth){
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        return userAuthRepository.save(userAuth);
    }


}
