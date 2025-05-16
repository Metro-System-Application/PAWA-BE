package pawa_be.ticket.external.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pawa_be.ticket.external.model.Schedule;

import java.util.List;

@Service
@Slf4j
public class ScheduleService {

    private final RestTemplate restTemplate;
    private final MetroLineService metroLineService;

    @Value("${external.metro-api.base-url:http://localhost:8081}")
    private String metroApiBaseUrl;

    @Autowired
    public ScheduleService(MetroLineService metroLineService) {
        this.restTemplate = new RestTemplate();
        this.metroLineService = metroLineService;
    }
    public List<Schedule> getScheduleByMetroLine(String metroLineId) {
        try {
            String token = metroLineService.authenticate();
            String url = metroApiBaseUrl + "/api/schedule/metro_line/" + metroLineId;

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
                        List<Schedule> schedules = objectMapper.readValue(
                                rawResponse.getBody(),
                                new TypeReference<List<Schedule>>() {
                                });

                        return schedules;
                    } catch (Exception e) {
                        log.error("Failed to parse schedule data: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse schedule data: " + e.getMessage());
                    }
                } else {
                    log.error("Failed to fetch schedule. Status code: {}", rawResponse.getStatusCode());
                    throw new RuntimeException(
                            "Failed to fetch schedule. Status code: " + rawResponse.getStatusCode());
                }
            } catch (HttpClientErrorException.NotFound e) {
                log.error("Metro line not found: {}", metroLineId);
                throw new RuntimeException("Metro line not found with id:" + metroLineId);
            } catch (Exception e) {
                log.error("Error communicating with schedule API: {}", e.getMessage());
                throw new RuntimeException("Error communicating with schedule API: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve schedule: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve schedule: " + e.getMessage());
        }
    }
} 