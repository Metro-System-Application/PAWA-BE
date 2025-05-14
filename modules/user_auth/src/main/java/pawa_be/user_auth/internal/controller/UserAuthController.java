package pawa_be.user_auth.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.jwt.config.HttpOnlyCookieConfig;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.profile.external.dto.RequestRegisterPassengerDTO;
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.user_auth.internal.dto.*;
import pawa_be.user_auth.internal.service.IUserAuthService;

import java.io.IOException;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.buildCookieWithCredentials;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User Auth Controller", description = "Operations about registration and authentication")
class UserAuthController {

    @Autowired
    private IUserAuthService userAuthService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Operation(summary = "Check user auth credentials before registration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email does not exist and is valid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Valid Email",
                                    summary = "Valid email input",
                                    value = "{\"success\": true, \"message\": \"\", \"data\": null}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Email Conflict",
                                    summary = "Email already exists",
                                    value = "{\"success\": false, \"message\": \"Email already exists\", \"data\": null}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email or password format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    summary = "Invalid input",
                                    value = "{\"success\": false, \"message\": \"Invalid email or password format\", \"data\": {\"email\": \"Sample email error\", \"password\": \"Sample password error\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    summary = "Internal error",
                                    value = "{\"success\": false, \"message\": \"Server error\", \"data\": null}"
                            )
                    )
            )
    })
    @GetMapping("/validate-existing-email")
    ResponseEntity<GenericResponseDTO<?>> validateLoginData(@Valid @RequestParam String email) {
        boolean emailExists = userAuthService.existsByEmail(email);
        return ResponseEntity
                .status(emailExists ? HttpStatus.CONFLICT : HttpStatus.OK)
                .body(new GenericResponseDTO<>(
                        !emailExists,
                        emailExists ? "Email already exists" : "",
                        null));
    }


    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Successful registration",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "User registered successfully",
                                      "data": {
                                        "userId": "abc123",
                                        "email": "user@example.com",
                                        "role": "PASSENGER"
                                      }
                                    }
                                    """
                            ))),
            @ApiResponse(responseCode = "400", description = "User already exists",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "User Exists",
                                    summary = "User already registered",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "User with this email already exists.",
                                      "data": null
                                    }
                                    """
                            ))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    summary = "Something went wrong",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "Internal server error",
                                      "data": null
                                    }
                                    """
                            )))
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponseDTO<?>> registerUser(@Valid @RequestBody RequestRegisterUserDTO user) {
        ResponseRegisterUserDTO response = userAuthService.registerUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericResponseDTO<>(true, "User registered successfully", response));
    }

    @Operation(summary = "Login a user and set auth token as HttpOnly cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = ResponseLoginUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/login")
    ResponseEntity<GenericResponseDTO<?>> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RequestLoginUserDTO loginDto
    ) {
        ResponseLoginUserDTO responseDto = userAuthService.login(
                loginDto.getEmail(),
                loginDto.getPassword()
        );

        String authCookieHeader = buildCookieWithCredentials(responseDto.getToken());

        response.setHeader("Set-Cookie", authCookieHeader);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDTO<>(true, "User logged in successfully", responseDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponseDTO<?>> logout(HttpServletResponse response) {
        Cookie logoutCookie = HttpOnlyCookieConfig.createCookie(
                UserAuthConfig.USER_AUTH_COOKIE_NAME,
                ""
        );
        logoutCookie.setMaxAge(0);
        logoutCookie.setPath("/");
        logoutCookie.setAttribute("SameSite", "None");

        response.addCookie(logoutCookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDTO<>(true, "User logged out successfully", null));
    }

    @PutMapping("/update-my-info")
    @Operation(summary = "Update user's email and/or password",
            description = "Authenticated users can update their email and password. If a new email is already in use, the request will fail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User info updated successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    })
    public ResponseEntity<GenericResponseDTO<?>> updateUserInfo(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            @Valid @RequestBody RequestUpdateUserDTO user
    ) {
        final String email = authentication.getName();

        String updatedUserEmail = userAuthService.updateUserCredentials(email, user);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(updatedUserEmail);
        String authToken = userAuthService.createAuthToken(userDetails, true);

        String authCookieHeader = buildCookieWithCredentials(authToken);

        response.setHeader("Set-Cookie", authCookieHeader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericResponseDTO<>(true, "Data updated successfully", null));
    }


    @GetMapping("/google-signup-url")
    @Operation(
            summary = "Get Google signup redirect URL",
            description = "Returns the Google OAuth signup URL that the frontend can use to redirect users for authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redirect URL returned successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    })
    ResponseEntity<GenericResponseDTO<?>> getRedirectLoginUrl() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDTO<>(true, "", new ResponseGetRedirectLoginUrlDTO(
                        userAuthService.buildGoogleSignUpUrl()
                )));
    }

    @GetMapping("/google")
    @Operation(
            summary = "Handle Google OAuth callback",
            description = "Handles the callback from Google OAuth. If the user is fully registered, returns an auth cookie. If the profile is incomplete, returns a temporary token and profile data."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "206",
                    description = "Profile not complete. Temporary token and profile data returned.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully authenticated. Auth token set in cookie.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or Google account already linked.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Google account already linked",
                                    value = """
            {
              "success": false,
              "message": "User with this email already registered regularly.",
              "data": null
            }
            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenericResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Server error",
                                    value = """
            {
              "success": false,
              "message": "Something went wrong. Please try again later.",
              "data": null
            }
            """
                            )
                    )
            )
    })
    public ResponseEntity<GenericResponseDTO<?>> handleGoogleCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("code") String code) throws IOException {
        ResponseGoogleAuthResultDTO result = userAuthService.authenticateAndHandleGoogleUser(code);

        if (!result.isProfileComplete()) {
            Cookie tempCookie = HttpOnlyCookieConfig.createCookie("TEMP_GOOGLE_AUTH", result.getTempToken());
            response.addCookie(tempCookie);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .body(new GenericResponseDTO<>(true, "Finish profile registration", result.getProfileData()));
        }

        String authCookieHeader = buildCookieWithCredentials(result.getAuthToken());

        response.setHeader("Set-Cookie", authCookieHeader);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDTO<>(true, "User logged in successfully", null));
    }


    @PostMapping("/fill-google-profile")
    @Operation(
            summary = "Complete Google OAuth registration",
            description = "Fills in the user's profile data after signing in with Google, using a temporary token from the cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile data successfully registered.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User associated with the token not found.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or expired/invalid token.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))
            )
    })
    ResponseEntity<GenericResponseDTO<?>> fillGoogleProfileData(
            @Valid @RequestBody RequestRegisterPassengerDTO profileData,
            @CookieValue(name = "TEMP_GOOGLE_AUTH") String tempToken) {
        ResponsePassengerDTO responsePassengerDTO = userAuthService.registerProfileFromGoogle(tempToken, profileData);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponseDTO<>(true, "Profile data is updated", responsePassengerDTO));
    }
}

