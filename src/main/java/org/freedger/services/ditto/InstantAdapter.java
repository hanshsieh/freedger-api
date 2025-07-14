package org.freedger.services.ditto;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Instant.from(FORMATTER.parse(json.getAsString()));
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Invalid date format. Expected format: yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS", e);
        }
    }

    @Override
    public JsonElement serialize(Instant instant, Type typeOfSrc, JsonSerializationContext context) {
        final var offsetDateTime = instant.atOffset(ZoneOffset.UTC);
        return new JsonPrimitive(FORMATTER.format(offsetDateTime));
    }
}
