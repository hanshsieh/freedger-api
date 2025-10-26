package org.freedger.repository.ditto.adapters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
  @Override
  public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    gen.writeStartObject();
    gen.writeStringField("strValue", value.toPlainString());
    gen.writeNumberField("numValue", value.doubleValue());
    gen.writeEndObject();
  }
}
