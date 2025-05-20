package pawa_be.infrastructure.google_oauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface IGoogleOAuthService {
    GoogleIdToken.Payload authenticateUser(String authorizationCode, boolean isLinking) throws IOException;
    String buildLoginUrl(boolean isLinking);
}
