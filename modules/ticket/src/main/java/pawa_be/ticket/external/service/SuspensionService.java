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
import pawa_be.ticket.external.model.Suspension;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SuspensionService {

    private final RestTemplate restTemplate;

    @Value("${external.metro-api.base-url:http://localhost:8081}")
    private String metroApiBaseUrl;

    public SuspensionService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Suspension> getAllSuspensions() {
        try {
            String url = metroApiBaseUrl + "/api/suspension/all";

            HttpHeaders headers = new HttpHeaders();
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
                        List<Suspension> suspensions = objectMapper.readValue(
                                rawResponse.getBody(),
                                new TypeReference<List<Suspension>>() {});

                        return suspensions;
                    } catch (Exception e) {
                        log.error("Failed to parse suspension data: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse suspension data: " + e.getMessage());
                    }
                } else {
                    log.error("Failed to fetch suspensions. Status code: {}", rawResponse.getStatusCode());
                    throw new RuntimeException(
                            "Failed to fetch suspensions. Status code: " + rawResponse.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error communicating with suspension API: {}", e.getMessage());
                throw new RuntimeException("Error communicating with suspension API: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve suspensions: {}", e.getMessage());
            return new ArrayList<>(); // Return empty list instead of throwing an exception
        }
    }
} 