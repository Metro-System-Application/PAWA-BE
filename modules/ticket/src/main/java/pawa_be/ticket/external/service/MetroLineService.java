package pawa_be.ticket.external.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawa_be.ticket.external.model.MetroLine;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.model.MetroStation;

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

    /**
     * Get a specific station by ID
     * Note: This is a simplified version of StationService.getStationById to avoid circular dependencies
     * 
     * @param stationId The ID of the station to retrieve
     * @return The station with the specified ID
     */
    public MetroStation getStationById(String stationId) {
        try {
            String token = authenticate();
            String url = metroApiBaseUrl + "/api/station/" + stationId;

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
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    try {
                        return objectMapper.readValue(rawResponse.getBody(), MetroStation.class);
                    } catch (Exception e) {
                        log.error("Failed to parse station data: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse station data: " + e.getMessage());
                    }
                } else {
                    log.error("Failed to fetch station. Status code: {}", rawResponse.getStatusCode());
                    throw new RuntimeException(
                            "Failed to fetch station. Status code: " + rawResponse.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error communicating with station API: {}", e.getMessage());
                throw new RuntimeException("Error communicating with station API: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve station: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve station: " + e.getMessage());
        }
    }

    public MetroLineResponse getMetroLineById(String metroLineId) {
        try {
            String token = authenticate();
            String url = metroApiBaseUrl + "/api/metro_line/" + metroLineId;

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
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    try {
                        log.info("Raw response body: {}", rawResponse.getBody());
                        
                        // The API returns a MetroLine object directly, not wrapped in a MetroLineResponse
                        MetroLine metroLine = objectMapper.readValue(
                                rawResponse.getBody(),
                                MetroLine.class);
                        
                        // Now we need to create the MetroLineResponse and find first and last stations
                        MetroLineResponse response = new MetroLineResponse();
                        response.setMetroLine(metroLine);
                        
                        // To populate first and last stations, we need to fetch those stations
                        if (metroLine.getStationOrder() != null && !metroLine.getStationOrder().isEmpty()) {
                            try {
                                String firstStationId = metroLine.getStationOrder().get(0);
                                String lastStationId = metroLine.getStationOrder().get(metroLine.getStationOrder().size() - 1);
                                
                                // Use our internal method to fetch station details
                                response.setFirstStation(getStationById(firstStationId));
                                response.setLastStation(getStationById(lastStationId));
                            } catch (Exception stationEx) {
                                log.warn("Could not fetch station details: {}", stationEx.getMessage());
                                // We'll continue even if we can't fetch station details
                            }
                        }
                        
                        return response;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse metro line data: " + e.getMessage());
                    }
                } else {
                    throw new RuntimeException(
                            "Failed to fetch metro line. Status code: " + rawResponse.getStatusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error communicating with metro line API: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve metro line: " + e.getMessage());
        }
    }
}
