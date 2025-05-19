package pawa_be.payment.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.model.MetroStation;
import pawa_be.ticket.external.service.MetroLineService;
import pawa_be.ticket.external.service.StationService;

import java.util.UUID;

@Service
public class TicketExternalService {

    @Autowired
    private StationService stationService;

    @Autowired
    private MetroLineService metroLineService;

    /**
     * Gets a station name by its UUID
     * @param stationId The UUID of the station
     * @return The station name or "Unknown Station" if not found
     */
    public String getStationName(UUID stationId) {
        if (stationId == null) {
            return "Unknown Station";
        }

        try {
            MetroStation station = stationService.getStationById(stationId.toString());
            return station.getName();
        } catch (Exception e) {
            return "Unknown Station";
        }
    }

    /**
     * Gets a metro line name by its ID
     * @param lineId The ID of the metro line
     * @return The metro line name or "Unknown Line" if not found
     */
    public String getLineName(String lineId) {
        if (lineId == null || lineId.isEmpty()) {
            return "Unknown Line";
        }

        try {
            MetroLineResponse line = metroLineService.getMetroLineById(lineId);
            // The metro line name can be accessed via the metroLine property
            return line.getMetroLine().getName();
        } catch (Exception e) {
            return "Unknown Line";
        }
    }
} 