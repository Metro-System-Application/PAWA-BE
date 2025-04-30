package pawa_be.cart.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart Controller", description = "Operations about cart")
class CartController {
    @GetMapping("")
    @Operation(summary = "Future cart controller", description = "Returns \"Hello, Cart!\" message.")
    String greet(){
        return "Hello, Cart!";
    }
}
