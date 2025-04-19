package pawa_be.payment.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@Tag(name = "Payment Controller", description = "Operations about Payment")
public class PaymentController {

    @GetMapping("")
    @Operation(summary = "Payment Module", description = "Returns \"Hello, Payment!\" message.")
    public String greet(){
        return "Hello, Payment!";
    }
}
