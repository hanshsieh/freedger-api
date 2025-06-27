package org.freedger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a token exchange request from the client.
 */
public class TokenExchangeRequest {
    
    @JsonProperty("token")
    private String token;

    // Default constructor for JSON deserialization
    public TokenExchangeRequest() {
    }

    public TokenExchangeRequest(String token) {
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
        return "TokenExchangeRequest{" +
                "token='" + token + '\'' +
                '}';
    }
}
