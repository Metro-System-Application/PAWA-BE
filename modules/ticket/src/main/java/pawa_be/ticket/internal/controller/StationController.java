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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.external.service.StationService;
import pawa_be.ticket.internal.dto.StationDto;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Station Controller", description = "Operations related to metro stations")
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @Operation(summary = "Get stations", description = "Returns metro stations, optionally filtered by metro line")
    @GetMapping("/stations")
    @ApiResponse(responseCode = "200", description = "Stations retrieved successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
    public ResponseEntity<GenericResponseDTO<List<StationDto>>> getStations(
            @RequestParam(required = false) String metroLineId) {
        try {
            List<StationDto> stations = stationService.getStationsDto(metroLineId);

            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            metroLineId == null ? "All stations retrieved successfully" 
                                    : "Stations for metro line retrieved successfully",
                            stations));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Failed to retrieve stations: " + e.getMessage(),
                            null));
        }
    }

    @Operation(summary = "Get station details", description = "Returns details of a specific station by ID")
    @GetMapping("/stations/{stationId}")
    @ApiResponse(responseCode = "200", description = "Station details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Station not found")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
    public ResponseEntity<GenericResponseDTO<StationDto>> getStationById(
            @PathVariable String stationId) {
        try {
            StationDto station = stationService.getStationDtoById(stationId);

            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            "Station details retrieved successfully",
                            station));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Station not found: " + e.getMessage(),
                            null));
        }
    }
}
