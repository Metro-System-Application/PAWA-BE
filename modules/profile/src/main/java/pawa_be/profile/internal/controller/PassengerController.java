package pawa_be.profile.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;
import pawa_be.profile.internal.dto.RequestUpdatePassengerDTO;
import pawa_be.profile.internal.dto.ResponsePassengerDTO;
import pawa_be.profile.internal.service.PassengerService;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile Controller", description = "Operations about passengers")
@RequiredArgsConstructor
public class PassengerController {

    @Autowired
    private final PassengerService passengerService;

    @GetMapping("/my-info")
    @Operation(
            summary = "Get passenger info",
            description = "Returns the currently logged-in passenger info"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> getPassengerInfo(
            @Parameter(hidden = true) Authentication authentication) {

        final String passengerId = getUserIdFromAuthentication(authentication);
        ResponsePassengerDTO passenger = passengerService.getCurrentPassengerById(passengerId);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info retrieved", passenger));
    }

    @PutMapping("/edit-my-info")
    @Operation(
            summary = "Update passenger info",
            description = "Edits the currently logged-in passenger info with optional fields"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger info updated successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> updatePassengerInfo(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Updated phone number and/or address") RequestUpdatePassengerDTO updatedInfo) {

        final String passengerId = getUserIdFromAuthentication(authentication);
        passengerService.updateCurrentPassengerById(passengerId, updatedInfo);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info updated", null));
    }
}
