package pawa_be.ticket.external.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawa_be.ticket.external.model.MetroLineResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MetroLineService {

    private final RestTemplate restTemplate;
    private static final String AUTH_EMAIL = "huuquoc7603@gmail.com";

    // Token caching mechanism
    private String cachedToken;
    private LocalDateTime tokenExpiration;

    @Value("${external.metro-api.base-url:http://localhost:8081}")
    private String metroApiBaseUrl;

    public MetroLineService() {
        this.restTemplate = new RestTemplate();
    }

    public String authenticate() {
        try {
            if (cachedToken != null && tokenExpiration != null && LocalDateTime.now().isBefore(tokenExpiration)) {
                return cachedToken;
            }

            String url = metroApiBaseUrl + "/api/auth/test/authenticate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity<String> requestEntity = new HttpEntity<>(AUTH_EMAIL, headers);

            try {
                ResponseEntity<String> rawResponse = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        String.class);

                String tokenResponse = rawResponse.getBody();
                if (tokenResponse != null && !tokenResponse.trim().isEmpty()) {
                    String token = tokenResponse.trim().replace("\"", "");
                    log.info("Successfully authenticated with external metro API");
                    cachedToken = token;
                    tokenExpiration = LocalDateTime.now().plusHours(1);

                    return cachedToken;
                } else {
                    log.error("Authentication response did not contain a token");
                    throw new RuntimeException("Authentication response did not contain a token");
                }
            } catch (Exception e) {
                log.error("Error during authentication exchange: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to authenticate: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with external metro API: " + e.getMessage());
        }
    }

    public List<MetroLineResponse> getAllMetroLines() {
        try {
            String token = authenticate();
            String url = metroApiBaseUrl + "/api/metro_line";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> rawResponse = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        String.class);

                if (rawResponse.getStatusCode() == HttpStatus.OK && rawResponse.getBody() != null) {
                    // Configure ObjectMapper for proper date/time handling
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                    try {
                        List<MetroLineResponse> metroLines = objectMapper.readValue(
                                rawResponse.getBody(),
                                new TypeReference<List<MetroLineResponse>>() {
                                });

                        return metroLines;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse metro lines data: " + e.getMessage());
                    }
                } else {
                    throw new RuntimeException(
                            "Failed to fetch metro lines. Status code: " + rawResponse.getStatusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error communicating with metro lines API: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve metro lines: " + e.getMessage());
        }
    }
}
