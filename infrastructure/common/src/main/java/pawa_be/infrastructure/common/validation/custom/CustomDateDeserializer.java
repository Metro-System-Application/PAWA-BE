package pawa_be.infrastructure.common.validation.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        try {
            return LocalDate.parse(p.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            String fieldName = ctxt.getParser().getCurrentName();
            if (fieldName == null && ctxt.getParser().getParsingContext() != null) {
                fieldName = ctxt.getParser().getParsingContext().getCurrentName();
            }

            String errorMessage = String.format(
                    "Field '%s' must be a date in format dd/MM/yyyy",
                    fieldName != null ? fieldName : "unknown"
            );

            throw new InvalidFormatException(p, errorMessage, p.getText(), LocalDate.class);
        }
    }
}

