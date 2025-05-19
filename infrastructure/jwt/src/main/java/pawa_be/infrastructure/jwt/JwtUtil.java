package pawa_be.infrastructure.jwt;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pawa_be.infrastructure.jwt.key.KeyStoreManager;
import pawa_be.infrastructure.jwt.user_details.CustomUserDetails;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final KeyStoreManager keyStoreManager;

    public JwtUtil(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager;
    }

    public String generateToken(CustomUserDetails userDetails) {
        try {
            PrivateKey privateKey = keyStoreManager.getPrivateKey();
            int TOKEN_VALIDITY = 60 * 60 * 24;

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userDetails.getUserId());
            claims.put("roles", userDetails.getAuthorities());

            return Jwts.builder()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuer("PAWA Backend")
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(Date.from(Instant.now().plusSeconds(TOKEN_VALIDITY)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error while signing JWT", e);
        }
    }

    public String generateTempTokenForGoogle(String googleId) {
        try {
            PrivateKey privateKey = keyStoreManager.getPrivateKey();
            int TEMP_TOKEN_VALIDITY = 15 * 60; // 15 minutes in seconds

            Map<String, Object> claims = new HashMap<>();
            claims.put("googleId", googleId);
            claims.put("type", "temp");

            return Jwts.builder()
                    .claims(claims)
                    .subject(googleId)
                    .issuer("PAWA Backend")
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(Date.from(Instant.now().plusSeconds(TEMP_TOKEN_VALIDITY)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error while signing temporary JWT", e);
        }
    }

    public String validateAndExtractGoogleIdFromTempToken(String token) {
        try {
            Claims claims = extractAllClaims(token, false);
            String type = (String) claims.get("type");

            if (!"temp".equals(type)) {
                throw new IllegalArgumentException("Token is not a temporary Google token");
            }

            if (isTokenExpired(token, false)) {
                throw new IllegalArgumentException("Temporary token has expired");
            }

            return claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid temporary token", e);
        }
    }

    private JwtParser loadJwtParser(boolean fromOpwa) {
        try {
            PublicKey publicKey = fromOpwa
                    ? KeyStoreManager.getOpwaPublicKey()
                    : keyStoreManager.getPublicKey();

            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
        } catch (SecurityException |
                 ExpiredJwtException |
                 MalformedJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key for JWT verification", e);
        }
    }

    private Claims extractAllClaims(String token, boolean fromOpwa)  {
        return loadJwtParser(fromOpwa)
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean fromOpwa) {
        final Claims claims = extractAllClaims(token, fromOpwa);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token, boolean fromOpwa) {
        return extractClaim(token, Claims::getSubject, fromOpwa);
    }

    private Date extractExpiration(String token, boolean fromOpwa) {
        return extractClaim(token, Claims::getExpiration, fromOpwa);
    }

    private boolean isTokenExpired(String token, boolean fromOpwa) {
        return extractExpiration(token, fromOpwa).before(new Date(System.currentTimeMillis()));
    }

    public boolean verifyJwtSignature(String token, boolean fromOpwa) {
        JwtParser parser = loadJwtParser(fromOpwa);
        return parser.isSigned(token) && !isTokenExpired(token, fromOpwa);
    }

    public String extractOpwaRole(String token) {
        try {
            Claims claims = extractAllClaims(token, true);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
