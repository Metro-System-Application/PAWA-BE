package pawa_be.ticket.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationDto {
    @Schema(description = "Station ID", example = "f57b9d3f-7d31-4f75-b2f3-4ff81dbd8cc3")
    private String id;

    @Schema(description = "Station name", example = "Ben Thanh")
    private String name;

    @Schema(description = "Station address", example = "Phường Phạm Ngũ Lão, Quận 1, Hồ Chí Minh, Vietnam")
    private String address;
}
