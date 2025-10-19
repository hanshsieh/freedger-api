package org.freedger.services.ditto;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalAdapter extends TypeAdapter<BigDecimal> {
  @Override
  public void write(JsonWriter out, BigDecimal value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name("strValue").value(value.toPlainString());
    out.name("numValue").value(value);
    out.endObject();
  }

  @Override
  public BigDecimal read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    in.beginObject();
    String strValue = null;
    while (in.hasNext()) {
      String name = in.nextName();
      if ("strValue".equals(name)) {
        strValue = in.nextString();
      } else {
        in.skipValue();
      }
    }
    in.endObject();
    if (strValue == null) {
      throw new JsonParseException("Missing \"strValue\" field, cannot deserialize BigDecimal");
    }
    return new BigDecimal(strValue);
  }
}
