package pawa_be.profile.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.profile.internal.dto.RequestUpdatePassengerDTO;
import pawa_be.profile.internal.dto.ResponsePassengerDTO;
import pawa_be.profile.internal.service.PassengerService;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile Controller", description = "Operations about passengers")
@RequiredArgsConstructor
public class PassengerController {
    @Autowired
    private final PassengerService passengerService;

    @GetMapping("/my-info")
    @Operation(summary = "Get passenger info", description = "Returns the currently logged-in passenger info")
    public ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> getPassengerInfo(
            Authentication authentication) {
        final String email = authentication.getName();
        ResponsePassengerDTO passenger = passengerService.getCurrentPassenger(email);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info retrieved", passenger));
    }

    @PutMapping("/edit-my-info")
    @Operation(summary = "Update passenger info", description = "Edits the currently logged-in passenger info")
    public ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> updatePassengerInfo(
            Authentication authentication,
            @Valid @RequestBody RequestUpdatePassengerDTO updatedInfo) {
        final String email = authentication.getName();
        passengerService.updateCurrentPassenger(email, updatedInfo);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info updated", null));
    }
}
