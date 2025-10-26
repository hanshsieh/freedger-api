package org.freedger.repository.ditto.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {
  @Override
  public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode node = p.getCodec().readTree(p);
    if (node.isNull()) {
      return null;
    }
    if (node.isObject()) {
      JsonNode strValueNode = node.get("strValue");
      if (strValueNode == null || strValueNode.isNull()) {
        throw new IOException("Missing \"strValue\" field, cannot deserialize BigDecimal");
      }
      return new BigDecimal(strValueNode.asText());
    }
    throw new IOException("Expected object with strValue field for BigDecimal deserialization");
  }
}
