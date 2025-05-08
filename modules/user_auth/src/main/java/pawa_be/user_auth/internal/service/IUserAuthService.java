package pawa_be.user_auth.internal.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.user_auth.internal.dto.*;

import java.io.IOException;

public interface IUserAuthService {
    boolean existsByEmail(String email);
    ResponseRegisterUserDTO registerUser(RequestRegisterUserDTO user);
    ResponseLoginUserDTO login(String email, String password);
    String updateUserCredentials(String currentEmail, RequestUpdateUserDTO userData);
    String buildGoogleSignUpUrl();
    ResponseGoogleAuthResultDTO authenticateAndHandleGoogleUser(String code) throws IOException;
    ResponsePassengerDTO registerProfileFromGoogle(String tempToken, RequestRegisterPassengerDTO profileData);
    String createAuthToken(CustomUserDetails user, boolean isValidCredential);
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
}
