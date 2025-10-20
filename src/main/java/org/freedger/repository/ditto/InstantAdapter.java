package org.freedger.repository.ditto;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantAdapter extends TypeAdapter<Instant> {
  // The formatter ensures the output is in ISO8601 format with exactly3 decimal places
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

  @Override
  public void write(JsonWriter out, Instant value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    final var offsetDateTime = OffsetDateTime.ofInstant(value, ZONE_OFFSET);
    out.value(FORMATTER.format(offsetDateTime));
  }

  @Override
  public Instant read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    try {
      return Instant.parse(in.nextString());
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Invalid ISO8601 date format", e);
    }
  }
}
