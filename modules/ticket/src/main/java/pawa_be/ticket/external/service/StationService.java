package pawa_be.ticket.external.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawa_be.ticket.external.model.MetroStation;
import pawa_be.ticket.internal.dto.StationDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StationService {

    private final RestTemplate restTemplate;
    private final MetroLineService metroLineService;

    @Value("${external.metro-api.base-url:http://localhost:8081}")
    private String metroApiBaseUrl;

    @Autowired
    public StationService(MetroLineService metroLineService) {
        this.restTemplate = new RestTemplate();
        this.metroLineService = metroLineService;
    }

    /**
     * Get all stations from the external API
     *
     * @return List of all stations
     */
    public List<MetroStation> getAllStations() {
        try {
            String token = metroLineService.authenticate();
            String url = metroApiBaseUrl + "/api/station";

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

                    try {
                        List<MetroStation> stations = objectMapper.readValue(
                                rawResponse.getBody(),
                                new TypeReference<List<MetroStation>>() {
                                });

                        return stations;
                    } catch (Exception e) {
                        log.error("Failed to parse station data: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse station data: " + e.getMessage());
                    }
                } else {
                    log.error("Failed to fetch stations. Status code: {}", rawResponse.getStatusCode());
                    throw new RuntimeException(
                            "Failed to fetch stations. Status code: " + rawResponse.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error communicating with station API: {}", e.getMessage());
                throw new RuntimeException("Error communicating with station API: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve stations: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve stations: " + e.getMessage());
        }
    }

    /**
     * Get a specific station by ID
     *
     * @param stationId The ID of the station to retrieve
     * @return The station with the specified ID
     */
    public MetroStation getStationById(String stationId) {
        try {
            String token = metroLineService.authenticate();
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

                    try {
                        MetroStation station = objectMapper.readValue(
                                rawResponse.getBody(),
                                MetroStation.class);

                        return station;
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

    /**
     * Convert station model to DTO with id, name, address, latitude and longitude
     * 
     * @param station Station model
     * @return Station DTO
     */
    public StationDto convertToDto(MetroStation station) {
        return new StationDto(
            station.getId(),
            station.getName(),
            station.getAddress(),
            station.getLatitude(),
            station.getLongitude()
        );
    }

    /**
     * Get all stations as DTOs
     * 
     * @return List of station DTOs
     */
    public List<StationDto> getAllStationsDto() {
        return getAllStations().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific station as DTO
     * 
     * @param stationId The ID of the station to retrieve
     * @return The station DTO
     */
    public StationDto getStationDtoById(String stationId) {
        return convertToDto(getStationById(stationId));
    }
}
