package org.freedger.dto.ditto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the incoming webhook request from Ditto for authentication.
 */
public class DittoWebhookRequest {
    @SerializedName("appID")
    private String appId;
    
    private String provider;
    private String token;

    // Getters and Setters
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
