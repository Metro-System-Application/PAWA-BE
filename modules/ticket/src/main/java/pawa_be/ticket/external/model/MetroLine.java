package pawa_be.ticket.external.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetroLine {
    private String id;
    private String name;
    private LocalTime firstArrival;
    private Integer trainFrequency;
    private Integer totalDuration;
    private List<String> stationOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
