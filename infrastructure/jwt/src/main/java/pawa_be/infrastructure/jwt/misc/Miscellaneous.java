package pawa_be.infrastructure.jwt.misc;

import org.springframework.security.core.Authentication;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;

public class Miscellaneous {
    public static String getUserIdFromAuthentication(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
