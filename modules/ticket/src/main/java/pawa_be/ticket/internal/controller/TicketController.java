package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket Controller", description = "Operations about tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("")
    @Operation(summary = "Ticket controller", description = "Returns \"Hello, Ticket!\" message.")
    public String greet(){
        return "Hello, Ticket!";
    }

    @Operation(summary = "Get all ticket types", description = "Returns all ticket types.")
    @GetMapping("/ticket-type")
    @ApiResponse(
            responseCode = "200", description = "All ticket types retrieved successfully"
    )
    @ApiResponse(
            responseCode = "401", description = "Unauthorized - user not authenticated"
    )
    @ApiResponse(
            responseCode = "500", description = "Internal server error"
    )
    public ResponseEntity<GenericResponseDTO<List<TypeDto>>> getAllTicketTypes(){
        List<TypeDto> ticketTypes = ticketService.getAllTicketType();
        return ResponseEntity.ok(
                new GenericResponseDTO<>(
                        true,
                        "Ticket types retrieved successfully",
                        ticketTypes
                )
        );
    }
}
