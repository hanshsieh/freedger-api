package org.freedger.controller.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.functions.HttpResponseMessage;
import java.time.OffsetDateTime;

/**
 * Custom serializer for handling function response serialization/deserialization.
 *
 * <p>Currently, Function App doesn't allow customizing the Gson instance for JSON serialization.
 * And Gson doesn't support data types like {@link OffsetDateTime} out of the box. This class
 * provides a custom serializer/deserializer for the missing parts.
 */
public class HttpMessageSerializer {
  private final Gson gson;

  public HttpMessageSerializer() {
    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();
  }

  public <T> T deserialize(String json, Class<T> clazz) {
    try {
      return gson.fromJson(json, clazz);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to deserialize JSON", e);
    }
  }

  public String serialize(Object object) {
    try {
      return gson.toJson(object);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to serialize JSON", e);
    }
  }

  public HttpResponseMessage.Builder serializeResponse(
      HttpResponseMessage.Builder builder, Object object) {
    return builder.body(serialize(object)).header("Content-Type", "application/json");
  }

  public HttpResponseMessage.Builder serializeResponse(
      HttpResponseMessage.Builder builder, Object object, String transactionId) {
    serializeResponse(builder, object);
    if (transactionId != null) {
      builder.header("X-TXN-ID", transactionId);
    }
    return builder;
  }
}
