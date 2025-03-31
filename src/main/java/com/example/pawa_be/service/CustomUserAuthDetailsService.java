package com.example.pawa_be.service;
import com.example.pawa_be.controller.CustomUserAuthDetails;
import com.example.pawa_be.model.UserAuth;
import com.example.pawa_be.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CustomUserAuthDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    public CustomUserAuthDetailsService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuth userAuth = userAuthRepository.findByEmail(email);
        if (Objects.nonNull(userAuth)) {
            System.out.println(  "User found: " + userAuth.getEmail() );
        }
        else {
            System.out.println(  "User not found: " + email );
        }
        return new CustomUserAuthDetails(userAuth);
    }
}
