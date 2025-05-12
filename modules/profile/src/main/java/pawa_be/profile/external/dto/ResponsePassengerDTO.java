package pawa_be.profile.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ResponsePassengerDTO {
    private String passengerFirstName;
    private String passengerMiddleName;
    private String passengerLastName;
    private String passengerPhone;
    private String passengerAddress;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate passengerDateOfBirth;
    private String nationalID;
    private String studentID;

    private Boolean hasDisability;
    private Boolean isRevolutionary;
}
