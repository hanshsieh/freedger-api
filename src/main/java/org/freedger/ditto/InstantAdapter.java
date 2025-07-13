package org.freedger.ditto;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;

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
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(FORMATTER.format(src));
    }
}
