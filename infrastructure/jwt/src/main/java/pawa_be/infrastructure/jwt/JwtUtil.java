package pawa_be.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
            int TOKEN_VALIDITY = 60 * 60 * 24; // One day validity

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
            Claims claims = extractAllClaims(token);
            String type = (String) claims.get("type");

            if (!"temp".equals(type)) {
                throw new IllegalArgumentException("Token is not a temporary Google token");
            }

            if (isTokenExpired(token)) {
                throw new IllegalArgumentException("Temporary token has expired");
            }

            return claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid temporary token", e);
        }
    }

    private JwtParser loadJwtParser() {
        try {
            PublicKey publicKey = keyStoreManager.getPublicKey();

            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error while verifying JWT", e);
        }
    }

    private Claims extractAllClaims(String token)  {
        return loadJwtParser()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public boolean verifyJwtSignature(String token) {
        JwtParser parser = loadJwtParser();
        return parser.isSigned(token) && !isTokenExpired(token);
    }
}
