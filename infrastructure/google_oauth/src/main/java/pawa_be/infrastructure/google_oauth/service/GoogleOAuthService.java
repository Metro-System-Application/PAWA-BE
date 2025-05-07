package pawa_be.infrastructure.google_oauth.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuthService implements IGoogleOAuthService {

    @Value("${google.oauth.client_id}")
    private String clientId;

    @Value("${google.oauth.client_secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect_uri_login}")
    private String redirectLoginUri;

    private static final NetHttpTransport transport = new NetHttpTransport();
    private static final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GoogleIdToken.Payload authenticateUser(String authorizationCode) throws IOException {
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                transport,
                jsonFactory,
                "https://oauth2.googleapis.com/token",
                clientId,
                clientSecret,
                authorizationCode,
                redirectLoginUri
        ).execute();

        String idTokenString = tokenResponse.getIdToken();

        GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, idTokenString);

        if (!idToken.verifyAudience(java.util.Collections.singletonList(clientId))) {
            throw new IllegalArgumentException("Invalid audience in ID token");
        }

        if (!idToken.verifyIssuer("https://accounts.google.com")) {
            throw new IllegalArgumentException("Invalid issuer in ID token");
        }

        return idToken.getPayload();
    }

    public String buildLoginUrl() {
        String encodedRedirectUri = URLEncoder.encode(redirectLoginUri, StandardCharsets.UTF_8);

        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + encodedRedirectUri +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline" +
                "&prompt=consent";
    }
}
