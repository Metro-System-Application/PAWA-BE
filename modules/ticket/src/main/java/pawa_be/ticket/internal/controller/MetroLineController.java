package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.service.MetroLineService;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Metro Line Controller", description = "Operations related to metro lines")
public class MetroLineController {

    private final MetroLineService metroLineService;

    @Autowired
    public MetroLineController(MetroLineService metroLineService) {
        this.metroLineService = metroLineService;
    }

    @Operation(summary = "Get all metro lines", description = "Returns all metro lines with their stations")
    @GetMapping("/metro-lines")
    @ApiResponse(responseCode = "200", description = "Metro lines retrieved successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
    public ResponseEntity<GenericResponseDTO<List<MetroLineResponse>>> getAllMetroLines() {
        List<MetroLineResponse> metroLines = metroLineService.getAllMetroLines();

        return ResponseEntity.ok(
                new GenericResponseDTO<>(
                        true,
                        "Metro lines retrieved successfully",
                        metroLines));
    }
    
    @Operation(summary = "Get metro line details", description = "Returns details of a specific metro line by ID")
    @GetMapping("/metro-lines/{metroLineId}")
    @ApiResponse(responseCode = "200", description = "Metro line details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Metro line not found")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
    public ResponseEntity<GenericResponseDTO<MetroLineResponse>> getMetroLineById(
            @PathVariable String metroLineId) {
        try {
            MetroLineResponse metroLine = metroLineService.getMetroLineById(metroLineId);

            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            "Metro line details retrieved successfully",
                            metroLine));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Metro line not found: " + e.getMessage(),
                            null));
        }
    }
}
