package pawa_be.user_auth.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.repository.UserAuthRepository;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService  {
    @Autowired
    private UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthModel user = userAuthRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        GrantedAuthority role = new SimpleGrantedAuthority(user.getRole().getRoleName());
        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                List.of(role)
        );
    }
}
