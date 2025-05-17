package pawa_be.ticket.external.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Suspension {
    private String id;
    private String metroLineID;
    private String title;
    private String description;
    private String suspensionType;
    private LocalDateTime expectedRestoreTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 