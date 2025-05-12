package pawa_be.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ResponseGetCartContentsDTO {
    List<CartContentDTO> cartContents;
}
