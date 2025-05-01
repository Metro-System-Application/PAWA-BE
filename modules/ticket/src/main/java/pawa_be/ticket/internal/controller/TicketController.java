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
@RequestMapping("/ticket")
@Tag(name = "Ticket Controller", description = "Operations about tickets")
class TicketController {

        private final TicketTypeService ticketTypeService;

        TicketController(TicketTypeService ticketTypeService) {
                this.ticketTypeService = ticketTypeService;
        }

        @Operation(summary = "Get ticket types", description = "Returns active ticket types, optionally filtered by passenger eligibility.")
        @GetMapping("/ticket-type")
        @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
        @ApiResponse(responseCode = "500", description = "Internal server error")
        ResponseEntity<GenericResponseDTO<List<TypeDto>>> getTicketTypes(
                        @RequestParam(required = false) String passengerId,
                        @RequestParam(required = false) String metroLineId) {

                List<TypeDto> ticketTypes;

                if (passengerId != null) {
                        // Filter by passenger eligibility
                        ticketTypes = ticketTypeService.getEligibleTicketTypesForPassenger(passengerId);
                } else {
                        // Get all active ticket types
                        ticketTypes = ticketTypeService.getAllTicketTypes();
                }

                // Additional filtering by metro line could be implemented here
                // if (metroLineId != null) { ... }

                return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                                true,
                                                "Ticket types retrieved successfully",
                                                ticketTypes));
        }
}
