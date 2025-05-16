package pawa_be.ticket.external.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDetail {
    private String metroLineId;
    private String metroLineName;
    private String stationId;
    private String stationName;
    private String stationAddress;
    private Integer arrivalDuration;
    private LocalTime arrivedAt;
} 