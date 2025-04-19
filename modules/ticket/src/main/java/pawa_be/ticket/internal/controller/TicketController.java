package pawa_be.ticket.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket Controller", description = "Operations about tickets")
public class TicketController {

    @GetMapping("")
    @Operation(summary = "Ticket controller", description = "Returns \"Hello, Ticket!\" message.")
    public String greet(){
        return "Hello, Ticket!";
    }
}
