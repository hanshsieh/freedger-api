package org.freedger.dto.ditto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response to Ditto's authentication webhook.
 * Follows the schema defined at: https://docs.ditto.live/sdk/latest/auth-and-authorization/data-authorization
 */
public class DittoWebhookResponse {
    @SerializedName("authenticated")
    private boolean authenticated;
    
    @SerializedName("userID")
    private String userId;
    
    @SerializedName("expirationSeconds")
    private Integer expirationSeconds;
    
    @SerializedName("permissions")
    private Permission permissions;
    
    @SerializedName("clientInfo")
    private Object clientInfo;
    
    @SerializedName("identityServiceMetadata")
    private Object identityServiceMetadata;
    
    // Default constructor for deserialization
    public DittoWebhookResponse() {
    }
    
    // Factory method for successful authentication
    public static DittoWebhookResponse success(String userId, int expirationSeconds, Permission permissions) {
        DittoWebhookResponse response = new DittoWebhookResponse();
        response.setAuthenticated(true);
        response.setUserId(userId);
        response.setExpirationSeconds(expirationSeconds);
        response.setPermissions(permissions);
        return response;
    }
    
    // Factory method for failed authentication
    public static DittoWebhookResponse failure() {
        DittoWebhookResponse response = new DittoWebhookResponse();
        response.setAuthenticated(false);
        return response;
    }

    // Getters and Setters
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(Integer expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public void setPermissions(Permission permissions) {
        this.permissions = permissions;
    }

    public Object getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(Object clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Object getIdentityServiceMetadata() {
        return identityServiceMetadata;
    }

    public void setIdentityServiceMetadata(Object identityServiceMetadata) {
        this.identityServiceMetadata = identityServiceMetadata;
    }
}
