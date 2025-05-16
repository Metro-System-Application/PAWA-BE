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
    @ApiResponse(responseCode = "200", description = "Schedule retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Metro line not found")
    @ApiResponse(responseCode = "500", description = "Internal server error or external service unavailable")
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
            if (e.getMessage().contains("Metro line not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new GenericResponseDTO<>(
                                false,
                                e.getMessage(),
                                null));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponseDTO<>(
                            false,
                            "Failed to retrieve schedule: " + e.getMessage(),
                            null));
        }
    }
} 