package org.freedger.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response containing the Ditto token.
 */
public class TokenExchangeResponse {
    
    @SerializedName("token")
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
