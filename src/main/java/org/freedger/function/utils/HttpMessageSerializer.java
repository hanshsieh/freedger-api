package org.freedger.function.utils;

import java.time.OffsetDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.functions.HttpResponseMessage;

/**
 * Custom serializer for handling function response serialization/deserialization.
 * 
 * Currently, Function App doesn't allow customizing the Gson instance for JSON serialization.
 * And Gson doesn't support data types like {@link OffsetDateTime} out of the box. This class
 * provides a custom serializer/deserializer for the missing parts.
 */
public class HttpMessageSerializer {
    private final Gson gson;
    
    public HttpMessageSerializer() {
     this.gson = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public String serialize(Object object) {
        return gson.toJson(object);
    }

    public HttpResponseMessage.Builder serializeResponse(HttpResponseMessage.Builder builder, Object object) {
        return builder.body(serialize(object))
            .header("Content-Type", "application/json");
    }

    public HttpResponseMessage.Builder serializeResponse(
        HttpResponseMessage.Builder builder, 
        Object object, 
        String transactionId) {
        serializeResponse(builder, object);
        if (transactionId != null) {
            builder.header("X-TXN-ID", transactionId);
        }
        return builder;
    }
}
