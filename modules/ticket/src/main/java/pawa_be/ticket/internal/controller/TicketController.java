package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.service.TicketTypeService;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Ticket Controller", description = "Operations about tickets")
class TicketController {

        private final TicketTypeService ticketTypeService;

        TicketController(TicketTypeService ticketTypeService) {
                this.ticketTypeService = ticketTypeService;
        }

        @Operation(summary = "Get all ticket types", description = "Returns all active ticket types")
        @GetMapping("/ticket-types")
        @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
        @ApiResponse(responseCode = "500", description = "Internal server error")
        ResponseEntity<GenericResponseDTO<List<TypeDto>>> getTicketTypes() {

                List<TypeDto> ticketTypes = ticketTypeService.getAllTicketTypes();

                return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                                true,
                                                "Ticket types retrieved successfully",
                                                ticketTypes));
        }

        @Operation(summary = "Get best ticket type for passenger", description = "Returns the most advantageous ticket type for a specific passenger based on eligibility (free tickets prioritized)")
        @GetMapping("/best-ticket")
        @ApiResponse(responseCode = "200", description = "Best ticket option retrieved successfully")
        @ApiResponse(responseCode = "404", description = "No eligible tickets found")
        @ApiResponse(responseCode = "400", description = "Missing required identification parameters")
        ResponseEntity<GenericResponseDTO<TypeDto>> getBestTicketForPassenger(
                        @RequestParam(required = true) String email) {

                if (email == null || email.trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "Email is required",
                                                        null));
                }

                TypeDto bestTicket = ticketTypeService.getBestTicketForPassengerByEmail(email);

                if (bestTicket == null) {
                        return ResponseEntity.status(404)
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "No eligible tickets found for this passenger",
                                                        null));
                }

                return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                                true,
                                                "Best ticket option found for passenger",
                                                bestTicket));
        }
}
