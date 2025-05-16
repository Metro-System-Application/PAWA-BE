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
import pawa_be.ticket.external.model.Schedule;
import pawa_be.ticket.external.service.ScheduleService;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Schedule Controller", description = "Operations related to metro schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "Get metro line schedule", description = "Returns schedule details for a specific metro line")
    @GetMapping("/schedules/metro-line/{metroLineId}")
    @ApiResponse(responseCode = "200", description = "Schedule retrieved successfully or error with message")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<GenericResponseDTO<List<Schedule>>> getScheduleByMetroLine(
            @PathVariable String metroLineId) {
        try {
            List<Schedule> schedules = scheduleService.getScheduleByMetroLine(metroLineId);

            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            "Schedule retrieved successfully",
                            schedules));
        } catch (Exception e) {
            // Simplify the error message by taking just the most relevant part
            String errorMsg = e.getMessage();
            if (errorMsg.contains(":")) {
                // Get the last part of the error message after the last colon
                String[] parts = errorMsg.split(":");
                errorMsg = parts[parts.length - 1].trim();
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponseDTO<>(
                            false,
                            errorMsg,
                            null));
        }
    }

    @Operation(summary = "Get schedules between stations", description = "Returns possible routes between two stations at a specified time")
    @GetMapping("/schedules/stations")
    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully or empty with message")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<GenericResponseDTO<List<Schedule>>> getSchedulesBetweenStations(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String dateTime) {
        
        if (start == null || start.isEmpty() || end == null || end.isEmpty() || dateTime == null || dateTime.isEmpty()) {
            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            false,
                            "Missing required parameters: start, end, and dateTime are required",
                            null));
        }
        
        try {
            List<Schedule> schedules = scheduleService.getScheduleBetweenStations(start, end, dateTime);
            
            if (schedules.isEmpty()) {
                return ResponseEntity.ok(
                        new GenericResponseDTO<>(
                                true,
                                "No routes found between the specified stations at the given time",
                                schedules));
            }
            
            return ResponseEntity.ok(
                    new GenericResponseDTO<>(
                            true,
                            "Schedules retrieved successfully",
                            schedules));
        } catch (Exception e) {
            // Simplify the error message by taking just the most relevant part
            String errorMsg = e.getMessage();
            if (errorMsg.contains(":")) {
                // Get the last part of the error message after the last colon
                String[] parts = errorMsg.split(":");
                errorMsg = parts[parts.length - 1].trim();
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponseDTO<>(
                            false,
                            errorMsg,
                            null));
        }
    }
} 