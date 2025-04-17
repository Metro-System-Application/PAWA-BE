package pawa_be.infrastructure.jwt.config;

import jakarta.servlet.http.Cookie;

public class HttpOnlyCookieConfig {
    private final static int COOKIE_AGE = 15 * 60; // 15 minutes

    public static Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(COOKIE_AGE);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(false); // No HTTPS for now
        return cookie;
    }
}

