package pawa_be.user_auth.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pawa_be.infrastructure.jwt.config.HttpOnlyCookieConfig;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.user_auth.internal.dto.DtoAuthResponse;
import pawa_be.user_auth.internal.dto.DtoLogin;
import pawa_be.user_auth.internal.model.UserAuthModel;
import pawa_be.user_auth.internal.service.UserAuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "User Auth Controller", description = "Operations about registration and authentication")
class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserAuthModel.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/register")
    public ResponseEntity<UserAuthModel> registerUser(@RequestBody UserAuthModel user) {

        UserAuthModel userWithHashedPassword = new UserAuthModel(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getRole()
        );

        UserAuthModel newUser = userService.createUser(userWithHashedPassword);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Login a user and set auth token as HttpOnly cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = DtoAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/login")
    public ResponseEntity<DtoAuthResponse> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody DtoLogin loginDto
    ) {

        String username = loginDto.getEmail();
        String password = loginDto.getPassword();

        try {
            UsernamePasswordAuthenticationToken credentialToken
                    = new UsernamePasswordAuthenticationToken(
                    username,
                    password
            );

            long startTime = System.currentTimeMillis();
            Authentication token = authenticationManager.authenticate(credentialToken);
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Authentication Duration: " + duration + "ms");
            System.out.println("User Authenticated: " + token.isAuthenticated());
            String userAuthToken = userService.createAuthToken(
                    (UserDetails) token.getPrincipal(),
                    token.isAuthenticated()
            );

            DtoAuthResponse responseDto = new DtoAuthResponse(userAuthToken);

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

