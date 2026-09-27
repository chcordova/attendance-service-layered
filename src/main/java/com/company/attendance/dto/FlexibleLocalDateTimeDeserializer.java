package com.company.attendance.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom Jackson deserializer for LocalDateTime that flexibly accepts: 1. Standard ISO-8601 local
 * date-time strings (e.g. "2026-09-26T08:30:00") 2. UTC / Offset ISO-8601 strings (e.g.
 * "2026-09-27T01:24:12.246Z", Swagger UI default)
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String text = parser.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();

        // 1. If it has timezone indicators (Z, z, +, or -HH:mm after the date), parse as
        // OffsetDateTime
        if (text.endsWith("Z") || text.endsWith("z") || text.contains("+") || isOffsetIso(text)) {
            try {
                return OffsetDateTime.parse(text).toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // fallback to ISO_LOCAL_DATE_TIME
            }
        }

        // 2. Parse standard local date time
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // 3. Fallback to generic ISO_DATE_TIME
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ex) {
                // 4. Fallback retry with OffsetDateTime in case of unusual zone format
                try {
                    return OffsetDateTime.parse(text).toLocalDateTime();
                } catch (DateTimeParseException finalEx) {
                    throw new IllegalArgumentException(
                            "Failed to parse timestamp '"
                                    + text
                                    + "'. Expected ISO-8601 format (e.g., '2026-09-26T08:30:00' or"
                                    + " '2026-09-27T01:24:12.246Z').",
                            finalEx);
                }
            }
        }
    }

    private boolean isOffsetIso(String text) {
        int lastMinus = text.lastIndexOf('-');
        // If there's a minus sign after the date part (after index 10, e.g.,
        // '2026-09-27T10:00:00-05:00')
        return lastMinus > 10;
    }
}
