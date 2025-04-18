package pawa_be.user_auth.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import pawa_be.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.jwt.config.HttpOnlyCookieConfig;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.config.UserRoleConfig;
import pawa_be.user_auth.internal.dto.RequestRegisterUserDTO;
import pawa_be.user_auth.internal.dto.ResponseLoginUserDTO;
import pawa_be.user_auth.internal.dto.RequestLoginUserDTO;
import pawa_be.user_auth.internal.dto.ResponseRegisterUserDTO;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.service.UserAuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "User Auth Controller", description = "Operations about registration and authentication")
class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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

        UserRoleConfig role = user.getRole() != null ? user.getRole() : UserRoleConfig.PASSENGER;
        UserAuthModel userWithHashedPassword = new UserAuthModel(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                role
        );

        UserAuthModel newUser = userAuthService.createUser(userWithHashedPassword);

        ResponseRegisterUserDTO responseData = new ResponseRegisterUserDTO(
                newUser.getUserId(),
                newUser.getEmail(),
                newUser.getRole()
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
    public ResponseEntity<ResponseLoginUserDTO> login(
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
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("Error when logging in: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

