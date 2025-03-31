package com.example.pawa_be.service;


import com.example.pawa_be.model.UserAuth;
import com.example.pawa_be.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;


    public UserAuth registerUser(UserAuth userAuth){
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        return userAuthRepository.save(userAuth);
    }

    public UserAuth findUserAuthByEmail(String email){
        return userAuthRepository.findByEmail(email);
    }

    public boolean validatePassword(String email, String rawPassword){
        UserAuth userAuth = userAuthRepository.findByEmail(email);
        return userAuth != null && passwordEncoder.matches(rawPassword, userAuth.getPassword());
    }

    public String loginUserWithEmail(UserAuth userAuthInstance){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userAuthInstance.getEmail(),
                        userAuthInstance.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {
            return "Login successful!\nToken: " + jwtService.generateToken(userAuthInstance);
        } return "Login failed!";
    }

    public UserAuth registerNewUser(UserAuth userAuthInstance){
        if (userAuthRepository.findByEmail(userAuthInstance.getEmail()) == null) {
            return userAuthRepository.save(userAuthInstance);
        } else {
            throw new RuntimeException("User already exists!");
        }
    }
}
