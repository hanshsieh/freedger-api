package org.freedger.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a token exchange request from the client.
 */
public class TokenExchangeRequest {
    
    @SerializedName("token")
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
