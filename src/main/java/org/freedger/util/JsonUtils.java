package org.freedger.util;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class for handling JSON serialization/deserialization and common HTTP responses.
 */
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Processes the request body and handles deserialization and validation.
     *
     * @param request The HTTP request
     * @param clazz The class to deserialize the JSON into
     * @param handler The function that processes the deserialized object
     * @param <T> The type of the request body
     * @param <R> The type of the response
     * @return HTTP response with the result or an error
     */
    public static <T, R> HttpResponseMessage processRequest(
            HttpRequestMessage<Optional<String>> request,
            Class<T> clazz,
            Function<T, R> handler) {
        
        // Check if request body is present
        if (!request.getBody().isPresent()) {
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Request body is required");
        }

        try {
            // Deserialize the request body
            T requestBody = objectMapper.readValue(request.getBody().get(), clazz);
           
            // Process the request using the provided handler
            R result = handler.apply(requestBody);
            
            // Return success response
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(result))
                    .build();
                    
        } catch (JsonProcessingException e) {
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Invalid JSON format");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            return createErrorResponse(request, HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Creates a standardized error response.
     */
    public static HttpResponseMessage createErrorResponse(
            HttpRequestMessage<?> request,
            HttpStatus status,
            String message) {
        
        ErrorResponse error = new ErrorResponse(status.toString(), message);
        final var responseBuilder = request.createResponseBuilder(status);
        
        try {
            return responseBuilder
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(error))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize error response", e);
        }
    }

    /**
     * Standard error response format.
     */
    public static class ErrorResponse {
        private final String status;
        private final String message;

        public ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
