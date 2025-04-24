package pawa_be.user_auth.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.jwt.JwtUtil;
import pawa_be.infrastructure.jwt.config.HttpOnlyCookieConfig;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;
import pawa_be.profile.external.service.ExternalPassengerService;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.user_auth.internal.dto.*;
import pawa_be.user_auth.internal.enumeration.UpdateUserResult;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.service.UserAuthService;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User Auth Controller", description = "Operations about registration and authentication")
class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ExternalPassengerService externalPassengerService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid input"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponseDTO<?>> registerUser(@Valid @RequestBody RequestRegisterUserDTO user) {
        if (userAuthService.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponseDTO<>(false, "User with this email already exists.", null));
        }

        UserAuthModel userWithHashedPassword = new UserAuthModel(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword())
        );

        UserAuthModel newUser = userAuthService.createUser(userWithHashedPassword);
        PassengerModel newPassenger = externalPassengerService.registerPassenger(
                newUser.getUserId(),
                newUser.getEmail(),
                user.getPassengerData()
        );

        ResponseRegisterUserDTO responseData = new ResponseRegisterUserDTO(
                newUser.getUserId(),
                newUser.getEmail(),
                UserRoleConfig.PASSENGER
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericResponseDTO<>(true, "User registered successfully", responseData));
    }

    @Operation(summary = "Login a user and set auth token as HttpOnly cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = ResponseLoginUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/login")
    public ResponseEntity<GenericResponseDTO<?>> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RequestLoginUserDTO loginDto
    ) {
        String username = loginDto.getEmail();
        String password = loginDto.getPassword();

        try {
            UsernamePasswordAuthenticationToken credentialToken
                    = new UsernamePasswordAuthenticationToken(
                    username,
                    password
            );

            Authentication token = authenticationManager.authenticate(credentialToken);
            String userAuthToken = userAuthService.createAuthToken(
                    (UserDetails) token.getPrincipal(),
                    token.isAuthenticated()
            );

            ResponseLoginUserDTO responseDto = new ResponseLoginUserDTO(userAuthToken);

            if (token.isAuthenticated()) {
                Cookie cookie = HttpOnlyCookieConfig.createCookie(
                        UserAuthConfig.USER_AUTH_COOKIE_NAME,
                        userAuthToken
                );
                response.addCookie(cookie);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new GenericResponseDTO<>(true, "User logged in successfully", responseDto));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new GenericResponseDTO<>(false, "Bad credentials.", null));
            }
        } catch (Exception e) {
            System.err.println("Error when logging in: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponseDTO<>(false, "Bad credentials.", null));
        }
    }

    @PutMapping("/update-my-info")
    public ResponseEntity<GenericResponseDTO<?>> updateUserInfo(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            @Valid @RequestBody RequestUpdateUserDTO user) {
        final String email = authentication.getName();

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        UpdateUserResult result = userAuthService.updateUserCredentials(email, user);

        try {
            switch (result.getStatus()) {
                case CURRENT_EMAIL_NOT_FOUND:
                    return ResponseEntity
                            .badRequest()
                            .body(new GenericResponseDTO<>(false, "User not found", null));
                case NEW_EMAIL_ALREADY_EXISTS:
                    return ResponseEntity
                            .badRequest()
                            .body(new GenericResponseDTO<>(false, "Email already in use", null));
                case SUCCESS: {
                        UserDetails userDetails = userAuthService.loadUserByUsername(user.getEmail());
                        String authToken = userAuthService.createAuthToken(userDetails, true);

                        Cookie cookie = HttpOnlyCookieConfig.createCookie(
                                UserAuthConfig.USER_AUTH_COOKIE_NAME,
                                authToken
                        );
                        response.addCookie(cookie);

                        return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(new GenericResponseDTO<>(true, "Data updated successfully", null));
                }
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponseDTO<>(false, "Error when updating info", null));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponseDTO<>(false, "Error when updating info", null));
    }
}

