package pawa_be.user_auth.internal.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import pawa_be.infrastructure.jwt.JwtUtil;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.repository.UserAuthRepository;

@Component
public class UserAuthService implements UserDetailsService  {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserAuthRepository userAuthRepository;

    public UserAuthModel createUser(UserAuthModel user) {
        return userAuthRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userAuthRepository.findByEmail(email) != null;
    }

    public String createAuthToken(UserDetails user, boolean isValidCredential) {
        if (isValidCredential) {
            return jwtUtil.generateToken(user);
        } else {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthModel user = userAuthRepository.findByEmail(email);

        if (user == null) {
            return new User(UserAuthConfig.USER_AUTH_INVALID_PLACEHOLDER,
                    "",
                    new ArrayList<GrantedAuthority>());
        } else {
            return User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().getRoleName())
                    .build();
        }

    }
}
