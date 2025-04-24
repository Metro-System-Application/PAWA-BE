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
        return extractExpiration(token).before(new Date());
    }

    public boolean verifyJwtSignature(String token) {
        JwtParser parser = loadJwtParser();
        return parser.isSigned(token) && !isTokenExpired(token);

    }
}
