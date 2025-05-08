package pawa_be.user_auth.internal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.common.validation.exceptions.*;
import pawa_be.infrastructure.google_oauth.service.IGoogleOAuthService;
import pawa_be.infrastructure.jwt.JwtUtil;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.payment.external.service.IExternalPaymentService;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.profile.external.service.IExternalPassengerService;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.user_auth.internal.dto.*;
import pawa_be.user_auth.internal.enumeration.UpdateUserAuthDataResult;
import pawa_be.user_auth.internal.enumeration.UpdateUserResult;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.repository.UserAuthRepository;

@Component
public class UserAuthService implements UserDetailsService, IUserAuthService  {

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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private UserAuthModel createUser(UserAuthModel user) {
        return userAuthRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userAuthRepository.findByEmail(email) != null;
    }

    private Optional<UserAuthModel> findByGoogleId(String googleId) { return userAuthRepository.findByGoogleId(googleId); }

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

    private UserDetails loadUserByGoogleId(String googleId) throws NotFoundException {
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

    public String updateUserCredentials(String currentEmail, RequestUpdateUserDTO userData) {
        if (userData.getPassword() != null) {
            userData.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        UserAuthModel currentUser = userAuthRepository.findByEmail(currentEmail);
        if (currentUser == null) {
            throw new NotFoundException("User not found");
        }

        final String newEmail = userData.getEmail();
        if (newEmail != null && !newEmail.isBlank()) {
            if (!currentEmail.equals(newEmail) && existsByEmail(newEmail)) {
                throw new AlreadyExistsException("Email already in use");
            }

            currentUser.setEmail(newEmail);
        }

        String newPassword = userData.getPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            currentUser.setPassword(newPassword);
        }

        try {
            return userAuthRepository.save(currentUser).getEmail();
        } catch (Exception e) {
            throw new RuntimeException("Error when updating user info");
        }
    }


    private ResponsePassengerDTO createProfileAndEWallet(String userId, RequestRegisterPassengerDTO profileData) {
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

    public ResponsePassengerDTO registerProfileFromGoogle(String tempToken, RequestRegisterPassengerDTO profileData) {
        String googleId = jwtUtil.validateAndExtractGoogleIdFromTempToken(tempToken);

        UserAuthModel registration = findByGoogleId(googleId).orElseThrow(
                () -> new NotFoundException("User with this googleId is not found")
        );

        return createProfileAndEWallet(registration.getUserId(), profileData);
    }

    public ResponseRegisterUserDTO registerUser(RequestRegisterUserDTO user) {
        if (existsByEmail(user.getEmail())) {
            throw new AlreadyExistsException("User with this email already exists.");
        }

        UserAuthModel userWithHashedPassword = UserAuthModel.fromPassword(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword())
        );

        UserAuthModel newUser = createUser(userWithHashedPassword);

        PassengerModel newPassenger;
        try {
            newPassenger = externalPassengerService.registerPassenger(newUser.getUserId(), user.getPassengerData());
        } catch (Exception e) {
            throw new RuntimeException("Failed to register passenger.");
        }

        try {
            externalPaymentService.createPassengerEwallet(newPassenger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create passenger e-wallet.");
        }

        return new ResponseRegisterUserDTO(
                newUser.getUserId(),
                newUser.getEmail(),
                UserRoleConfig.PASSENGER
        );
    }

    private String getLoginToken(String username, String password) {
        UsernamePasswordAuthenticationToken credentialToken
                = new UsernamePasswordAuthenticationToken(
                username,
                password
        );

        Authentication token = authenticationManager.authenticate(credentialToken);
        return createAuthToken(
                (CustomUserDetails) token.getPrincipal(),
                token.isAuthenticated()
        );
    }

    public ResponseLoginUserDTO login(String email, String password) {
        try {
            String token = getLoginToken(email, password);
            return new ResponseLoginUserDTO(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Bad credentials.");
        }
    }

    public ResponseGoogleAuthResultDTO authenticateAndHandleGoogleUser(String code) throws IOException {
        GoogleIdToken.Payload payload = googleOAuthService.authenticateUser(code);

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new GoogleEmailNotVerifiedException("Email is not verified by Google");
        }

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        Optional<UserAuthModel> user = findByGoogleId(googleId);

        UserAuthModel resolvedUser;
        if (user.isEmpty()) {
            if (existsByEmail(email)) {
                throw new GoogleAccountAlreadyLinkedException("User with this email already registered regularly.");
            }

            resolvedUser = UserAuthModel.fromGoogleId(email, googleId);
            createUser(resolvedUser);
        } else {
            resolvedUser = user.get();
        }

        boolean profileFilled = externalPassengerService.checkIsPassengerProfileIsFilled(resolvedUser.getUserId());

        if (!profileFilled) {
            String tempToken = jwtUtil.generateTempTokenForGoogle(googleId);
            return new ResponseGoogleAuthResultDTO(false, null, tempToken,
                    new ResponseGoogleProfileDataDTO(
                        (String) payload.get("given_name"),
                        (String) payload.get("family_name")
                    )
            );
        }

        CustomUserDetails userDetails = (CustomUserDetails) loadUserByGoogleId(googleId);
        String authToken = createAuthToken(userDetails, true);

        return new ResponseGoogleAuthResultDTO(true, authToken, null, null);
    }

    public String buildGoogleSignUpUrl() {
        return googleOAuthService.buildLoginUrl();
    }
}
