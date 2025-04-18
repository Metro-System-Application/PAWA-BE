package pawa_be.profile.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@Tag(name = "Profile Controller", description = "Operations about passengers")
public class PassengerController {

    @GetMapping("")
    @Operation(summary = "Greet passenger", description = "Returns \"Hello, Passenger!\" message.")
    public String greet(){
        return "Hello, Passenger!";
    }
}
