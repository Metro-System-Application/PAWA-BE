package pawa_be.ticket.external.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetroLineResponse {
    private MetroLine metroLine;
    private MetroStation firstStation;
    private MetroStation lastStation;
}
