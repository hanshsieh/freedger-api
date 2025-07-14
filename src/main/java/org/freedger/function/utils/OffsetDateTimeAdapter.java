package org.freedger.function.utils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for java.time.OffsetDateTime
 * Serializes/deserializes OffsetDateTime to/from ISO-8601 format
 */
public class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(FORMATTER.format(value));
    }

    @Override
    public OffsetDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        
        try {
            String dateString = in.nextString();
            if (dateString == null || dateString.isEmpty()) {
                return null;
            }
            return OffsetDateTime.parse(dateString, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IOException("Failed to parse OffsetDateTime: " + e.getMessage(), e);
        }
    }
}
