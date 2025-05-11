package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.service.MetroLineService;

import java.util.List;

@RestController
@RequestMapping("/ticket")
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
}
