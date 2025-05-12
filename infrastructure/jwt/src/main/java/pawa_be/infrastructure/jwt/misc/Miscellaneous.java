package pawa_be.infrastructure.jwt.misc;

import jakarta.servlet.http.Cookie;
import org.springframework.security.core.Authentication;
import pawa_be.infrastructure.jwt.config.HttpOnlyCookieConfig;
import pawa_be.infrastructure.jwt.config.UserAuthConfig;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;

public class Miscellaneous {
    public static String getUserIdFromAuthentication(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    public static String getEmailFromAuthentication(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    public static String buildCookieWithCredentials(String token) {
        Cookie cookie = HttpOnlyCookieConfig.createCookie(
                UserAuthConfig.USER_AUTH_COOKIE_NAME,
                token
        );

        return String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                cookie.getName(), cookie.getValue(), cookie.getMaxAge());
    }
}
