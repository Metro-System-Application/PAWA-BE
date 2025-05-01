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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket Controller", description = "Operations about tickets")
class TicketController {

        private final TicketTypeService ticketTypeService;

        TicketController(TicketTypeService ticketTypeService) {
                this.ticketTypeService = ticketTypeService;
        }

        @Operation(summary = "Get ticket types", description = "Returns active ticket types with optional filtering by passenger eligibility, budget, expiry time, and metro line.")
        @GetMapping("/ticket-type")
        @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
        @ApiResponse(responseCode = "400", description = "Invalid parameter")
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
        @ApiResponse(responseCode = "500", description = "Internal server error")
        ResponseEntity<GenericResponseDTO<List<TypeDto>>> getTicketTypes(
                        @RequestParam(required = false) String passengerId,
                        @RequestParam(required = false) BigDecimal price,
                        @RequestParam(required = false) Long expiryHours,
                        @RequestParam(required = false) String metroLineId) {

                // Validate price if provided
                if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
                        return ResponseEntity.badRequest()
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "Price must be a non-negative value",
                                                        null));
                }

                // Validate expiryHours if provided
                if (expiryHours != null && expiryHours <= 0) {
                        return ResponseEntity.badRequest()
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "Expiry hours must be a positive value",
                                                        null));
                }

                List<TypeDto> ticketTypes;

                // Step 1: Get base ticket types (all or by passenger eligibility)
                if (passengerId != null) {
                        // Filter by passenger eligibility
                        ticketTypes = ticketTypeService.getEligibleTicketTypesForPassenger(passengerId);
                } else {
                        // Get all active ticket types
                        ticketTypes = ticketTypeService.getAllTicketTypes();
                }

                // Step 2: Apply price filter if provided
                if (price != null) {
                        ticketTypes = ticketTypes.stream()
                                        .filter(ticket -> ticket.getPrice().compareTo(price) <= 0)
                                        .collect(java.util.stream.Collectors.toList());
                }

                // Step 3: Apply expiry hours filter if provided
                if (expiryHours != null) {
                        ticketTypes = ticketTypes.stream()
                                        .filter(ticket -> ticket.getExpiryInterval().toHours() <= expiryHours)
                                        .collect(java.util.stream.Collectors.toList());
                }

                // Step 4: Additional filtering by metro line could be implemented here
                // if (metroLineId != null) { ... }

                String message = "Ticket types retrieved successfully";
                if (price != null && expiryHours != null) {
                        message = "Available tickets within your budget and timeframe retrieved successfully";
                } else if (price != null) {
                        message = "Available tickets within your budget retrieved successfully";
                } else if (expiryHours != null) {
                        message = "Available tickets with your desired expiry time retrieved successfully";
                }

                return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                                true,
                                                message,
                                                ticketTypes));
        }
}
