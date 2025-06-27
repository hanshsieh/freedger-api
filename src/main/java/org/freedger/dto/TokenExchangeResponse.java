package org.freedger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response containing the Ditto token.
 */
public class TokenExchangeResponse {
    
    @JsonProperty("token")
    private String token;

    // Default constructor for JSON deserialization
    public TokenExchangeResponse() {
    }

    public TokenExchangeResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenExchangeResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}
