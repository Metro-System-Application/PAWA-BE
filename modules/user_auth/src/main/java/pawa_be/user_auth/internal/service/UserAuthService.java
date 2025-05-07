package pawa_be.user_auth.internal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.infrastructure.google_oauth.service.IGoogleOAuthService;
import pawa_be.infrastructure.jwt.JwtUtil;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.payment.external.service.IExternalPaymentService;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.profile.external.service.IExternalPassengerService;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.user_auth.internal.dto.RequestUpdateUserDTO;
import pawa_be.user_auth.internal.enumeration.UpdateUserAuthDataResult;
import pawa_be.user_auth.internal.enumeration.UpdateUserResult;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.repository.UserAuthRepository;

@Component
public class UserAuthService implements UserDetailsService  {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private IExternalPassengerService externalPassengerService;

    @Autowired
    private IExternalPaymentService externalPaymentService;

    @Autowired
    private IGoogleOAuthService googleOAuthService;

    public UserAuthModel createUser(UserAuthModel user) {
        return userAuthRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userAuthRepository.findByEmail(email) != null;
    }

    public Optional<UserAuthModel> findByGoogleId(String googleId) { return userAuthRepository.findByGoogleId(googleId); }

    public String createAuthToken(CustomUserDetails user, boolean isValidCredential) {
        if (isValidCredential) {
            return jwtUtil.generateToken(user);
        } else {
            throw new RuntimeException("Couldn't create auth token.");
        }
    }

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

    public UserDetails loadUserByGoogleId(String googleId) throws NotFoundException {
        UserAuthModel user = userAuthRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new NotFoundException("User with this googleId not found."));

        GrantedAuthority role = new SimpleGrantedAuthority(user.getRole().getRoleName());
        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                List.of(role)
        );
    }

    public UpdateUserResult updateUserCredentials(String currentEmail, RequestUpdateUserDTO userData) {
        UserAuthModel currentUser = userAuthRepository.findByEmail(currentEmail);
        if (currentUser == null) {
            return new UpdateUserResult(UpdateUserAuthDataResult.CURRENT_EMAIL_NOT_FOUND, null);
        }

        final String newEmail = userData.getEmail();
        if (newEmail != null && !newEmail.isBlank()) {
            if (!currentEmail.equals(newEmail) && existsByEmail(newEmail)) {
                return new UpdateUserResult(UpdateUserAuthDataResult.NEW_EMAIL_ALREADY_EXISTS, null);
            }

            currentUser.setEmail(newEmail);
        }

        String newPassword = userData.getPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            currentUser.setPassword(newPassword);
        }

        UserAuthModel updatedUser = userAuthRepository.save(currentUser);
        return new UpdateUserResult(UpdateUserAuthDataResult.SUCCESS, updatedUser);
    }

    public ResponsePassengerDTO createProfileAndEWallet(String userId, RequestRegisterPassengerDTO profileData) {
        PassengerModel newPassenger = externalPassengerService.registerPassenger(
                userId,
                profileData
        );

        externalPaymentService.createPassengerEwallet(newPassenger);

        return new ResponsePassengerDTO(
                newPassenger.getPassengerFirstName(),
                newPassenger.getPassengerMiddleName(),
                newPassenger.getPassengerLastName(),
                newPassenger.getPassengerPhone(),
                newPassenger.getPassengerAddress(),
                newPassenger.getPassengerDateOfBirth(),
                newPassenger.getNationalID(),
                newPassenger.getStudentID(),
                newPassenger.getHasDisability(),
                newPassenger.getIsRevolutionary()
        );
    }

    public ResponsePassengerDTO registerProfileFromGoogle(String googleId, RequestRegisterPassengerDTO profileData) {
        UserAuthModel registration = findByGoogleId(googleId).orElseThrow(
                () -> new NotFoundException("User with this googleId is not found")
        );

        return createProfileAndEWallet(registration.getUserId(), profileData);
    }
}
