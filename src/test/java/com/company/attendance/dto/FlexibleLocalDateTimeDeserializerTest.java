package com.company.attendance.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlexibleLocalDateTimeDeserializerTest {

    private FlexibleLocalDateTimeDeserializer deserializer;
    private JsonParser jsonParser;
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        deserializer = new FlexibleLocalDateTimeDeserializer();
        jsonParser = mock(JsonParser.class);
        context = mock(DeserializationContext.class);
    }

    @Test
    @DisplayName("Should deserialize standard ISO local date-time string")
    void shouldDeserializeStandardIsoLocalDateTime() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-26T08:30:00");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 26, 8, 30, 0));
    }

    @Test
    @DisplayName("Should deserialize ISO local date-time string with fractional seconds")
    void shouldDeserializeIsoLocalDateTimeWithFractionalSeconds() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-26T08:30:00.123");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 26, 8, 30, 0, 123_000_000));
    }

    @Test
    @DisplayName("Should deserialize UTC string with 'Z' as in Swagger UI default")
    void shouldDeserializeUtcStringWithZ() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-27T01:24:12.246Z");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 27, 1, 24, 12, 246_000_000));
    }

    @Test
    @DisplayName("Should deserialize UTC string with 'Z' without fractional seconds")
    void shouldDeserializeUtcStringWithZWithoutFractionalSeconds() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-27T01:24:12Z");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 27, 1, 24, 12));
    }

    @Test
    @DisplayName("Should deserialize offset date-time string with positive offset")
    void shouldDeserializeOffsetDateTimeWithPositiveOffset() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-27T08:30:00+02:00");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 27, 8, 30, 0));
    }

    @Test
    @DisplayName("Should deserialize offset date-time string with negative offset")
    void shouldDeserializeOffsetDateTimeWithNegativeOffset() throws IOException {
        when(jsonParser.getText()).thenReturn("2026-09-27T08:30:00-05:00");

        LocalDateTime result = deserializer.deserialize(jsonParser, context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 27, 8, 30, 0));
    }

    @Test
    @DisplayName("Should return null when parser returns null or empty string")
    void shouldReturnNullOnNullOrEmptyInput() throws IOException {
        when(jsonParser.getText()).thenReturn(null);
        assertThat(deserializer.deserialize(jsonParser, context)).isNull();

        when(jsonParser.getText()).thenReturn("   ");
        assertThat(deserializer.deserialize(jsonParser, context)).isNull();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when format is completely invalid")
    void shouldThrowOnInvalidFormat() throws IOException {
        when(jsonParser.getText()).thenReturn("not-a-valid-date");

        assertThatThrownBy(() -> deserializer.deserialize(jsonParser, context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse timestamp 'not-a-valid-date'");
    }
}
