package pawa_be.ticket.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspensionMessage {
    private String id;
    private String title;
    private String description;
    private String suspensionType;
    private String expectedRestoreTime;
} 