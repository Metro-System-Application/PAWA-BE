package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.service.TicketTypeService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket Controller", description = "Operations about tickets")
class TicketController {

        private final TicketTypeService ticketTypeService;

        TicketController(TicketTypeService ticketTypeService) {
                this.ticketTypeService = ticketTypeService;
        }

        @Operation(summary = "Get all ticket types for guests", 
                  description = "Returns filtered ticket types for guests based on the metro line: ONE_WAY (based on metro line), DAILY, THREE_DAY, MONTHLY_ADULT")
        @GetMapping("/ticket-types")
        @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
        @ApiResponse(responseCode = "400", description = "Missing required parameter")
        @ApiResponse(responseCode = "500", description = "Internal server error")
        ResponseEntity<GenericResponseDTO<List<TypeDto>>> getTicketTypes(
                @RequestParam(required = true) String metroLineId) {
                try {
                        if (metroLineId == null || metroLineId.trim().isEmpty()) {
                                return ResponseEntity.ok(
                                        new GenericResponseDTO<>(
                                                false,
                                                "Metro line ID is required",
                                                new ArrayList<>()));
                        }

                        List<TypeDto> ticketTypes = ticketTypeService.getGuestTicketTypes(metroLineId);

                        return ResponseEntity.ok(
                                        new GenericResponseDTO<>(
                                                        true,
                                                        "Ticket types retrieved successfully",
                                                        ticketTypes));
                } catch (Exception e) {
                        return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                        false,
                                        "Error retrieving ticket types: " + e.getMessage(),
                                        new ArrayList<>()));
                }
        }

        @Operation(summary = "Get best ticket types for passenger", 
                  description = "Returns the most advantageous ticket types for a specific passenger based on eligibility: " +
                              "Eligible users get FREE ticket, Students get student-specific tickets, Normal users get standard tickets")
        @GetMapping("/best-ticket")
        @ApiResponse(responseCode = "200", description = "Best ticket options retrieved successfully")
        @ApiResponse(responseCode = "400", description = "Missing required parameters")
        ResponseEntity<GenericResponseDTO<List<TypeDto>>> getBestTicketForPassenger(
                        @RequestParam(required = true) String email,
                        @RequestParam(required = true) String metroLineId) {

                if (email == null || email.trim().isEmpty()) {
                        return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                        false,
                                        "Email is required",
                                        new ArrayList<>()));
                }

                if (metroLineId == null || metroLineId.trim().isEmpty()) {
                        return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                        false,
                                        "Metro line ID is required",
                                        new ArrayList<>()));
                }

                try {
                        List<TypeDto> bestTickets = ticketTypeService.getBestTicketsForPassengerWithMetroLine(
                                email, metroLineId);
                        return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                        true,
                                        "Best ticket options retrieved successfully",
                                        bestTickets));
                } catch (Exception e) {
                        return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                        false,
                                        "Error retrieving ticket options: " + e.getMessage(),
                                        new ArrayList<>()));
                }
        }
}
