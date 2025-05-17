package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.external.model.Suspension;
import pawa_be.ticket.external.service.SuspensionService;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Suspension Controller", description = "Operations related to metro service suspensions")
public class SuspensionController {

    private final SuspensionService suspensionService;

    @Autowired
    public SuspensionController(SuspensionService suspensionService) {
        this.suspensionService = suspensionService;
    }

    @Operation(summary = "Get all suspensions", description = "Returns all metro service suspensions")
    @GetMapping("/suspensions")
    @ApiResponse(responseCode = "200", description = "Suspensions retrieved successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
    public ResponseEntity<GenericResponseDTO<List<Suspension>>> getAllSuspensions() {
        try {
            List<Suspension> suspensions = suspensionService.getAllSuspensions();

            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            "Suspensions retrieved successfully",
                            suspensions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Failed to retrieve suspensions: " + e.getMessage(),
                            null));
        }
    }
} 