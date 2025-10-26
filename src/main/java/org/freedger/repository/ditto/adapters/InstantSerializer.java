package org.freedger.repository.ditto.adapters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends JsonSerializer<Instant> {
  // The formatter ensures the output is in ISO8601 format with exactly 3 decimal places
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

  @Override
  public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    final var offsetDateTime = OffsetDateTime.ofInstant(value, ZONE_OFFSET);
    gen.writeString(FORMATTER.format(offsetDateTime));
  }
}
