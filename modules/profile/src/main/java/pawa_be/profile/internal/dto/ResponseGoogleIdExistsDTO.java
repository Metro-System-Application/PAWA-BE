package pawa_be.profile.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ResponseGoogleIdExistsDTO {
    private boolean linked;
}
