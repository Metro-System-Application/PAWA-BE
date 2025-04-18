package internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@Tag(name = "Image Controller", description = "Operations about Image")
public class CartController {

    @GetMapping("")
    @Operation(summary = "Greet Image", description = "Returns \"Hello, Passenger!\" message.")
    public String greet(){
        return "Hello, Passenger!";
    }
}
