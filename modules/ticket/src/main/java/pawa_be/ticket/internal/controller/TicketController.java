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

        @Operation(summary = "Get best ticket type for passenger", description = "Returns the most advantageous ticket type for a specific passenger based on eligibility (free tickets prioritized)")
        @GetMapping("/best-ticket")
        @ApiResponse(responseCode = "200", description = "Best ticket option retrieved successfully")
        @ApiResponse(responseCode = "404", description = "No eligible tickets found")
        @ApiResponse(responseCode = "400", description = "Missing required passenger ID")
        ResponseEntity<GenericResponseDTO<TypeDto>> getBestTicketForPassenger(
                        @RequestParam String passengerId) {

                if (passengerId == null || passengerId.trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "Passenger ID is required",
                                                        null));
                }

                TypeDto bestTicket = ticketTypeService.getBestTicketForPassenger(passengerId);

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

        @Operation(summary = "Get best ticket type by attributes", description = "Returns the best ticket type available based on specific passenger attributes")
        @GetMapping("/eligible-ticket")
        @ApiResponse(responseCode = "200", description = "Best eligible ticket found")
        @ApiResponse(responseCode = "404", description = "No eligible special ticket found")
        ResponseEntity<GenericResponseDTO<TypeDto>> getBestTicketByAttributes(
                        @RequestParam(required = false) Boolean isRevolutionary,
                        @RequestParam(required = false) Boolean hasDisability,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) String studentId) {

                // Build message based on provided attributes
                StringBuilder attributeMessage = new StringBuilder("Based on attributes: ");
                boolean hasAttributes = false;

                if (Boolean.TRUE.equals(isRevolutionary)) {
                        attributeMessage.append("revolutionary status, ");
                        hasAttributes = true;
                }

                if (Boolean.TRUE.equals(hasDisability)) {
                        attributeMessage.append("disability status, ");
                        hasAttributes = true;
                }

                if (age != null) {
                        attributeMessage.append("age " + age + ", ");
                        hasAttributes = true;
                }

                if (studentId != null && !studentId.trim().isEmpty()) {
                        attributeMessage.append("student status, ");
                        hasAttributes = true;
                }

                // Check if any attributes were provided
                if (!hasAttributes) {
                        return ResponseEntity.ok(
                                        new GenericResponseDTO<>(
                                                        true,
                                                        "No special eligibility attributes provided, showing standard ticket options",
                                                        null));
                }

                // Get the best ticket based on the attributes
                TypeDto bestTicket = ticketTypeService.getBestTicketByAttributes(
                                isRevolutionary, hasDisability, age, studentId);

                if (bestTicket == null) {
                        return ResponseEntity.status(404)
                                        .body(new GenericResponseDTO<>(
                                                        false,
                                                        "No specific ticket available for the provided attributes",
                                                        null));
                }

                return ResponseEntity.ok(
                                new GenericResponseDTO<>(
                                                true,
                                                attributeMessage.toString() + "the best ticket option is: "
                                                                + bestTicket.getTypeName(),
                                                bestTicket));
        }
}
