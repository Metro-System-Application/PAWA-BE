package pawa_be.infrastructure.jwt.config;

import jakarta.servlet.http.Cookie;

public class HttpOnlyCookieConfig {
    private final static int COOKIE_AGE = 60 * 60; // 60 minutes

    public static Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(COOKIE_AGE);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        return cookie;
    }
}

