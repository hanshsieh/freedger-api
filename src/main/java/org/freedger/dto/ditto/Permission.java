package org.freedger.dto.ditto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the permissions object in the Ditto webhook response.
 */
public class Permission {
    @SerializedName("read")
    private PermissionRules read;
    
    @SerializedName("write")
    private PermissionRules write;
    
    @SerializedName("remoteQuery")
    private Boolean remoteQuery;
    
    public Permission() {
        this.read = new PermissionRules();
        this.write = new PermissionRules();
    }
    
    public Permission(PermissionRules read, PermissionRules write) {
        this.read = read != null ? read : new PermissionRules();
        this.write = write != null ? write : new PermissionRules();
    }
    
    // Getters and Setters
    public PermissionRules getRead() {
        return read;
    }

    public void setRead(PermissionRules read) {
        this.read = read != null ? read : new PermissionRules();
    }

    public PermissionRules getWrite() {
        return write;
    }

    public void setWrite(PermissionRules write) {
        this.write = write != null ? write : new PermissionRules();
    }
    
    public Boolean getRemoteQuery() {
        return remoteQuery;
    }
    
    public void setRemoteQuery(Boolean remoteQuery) {
        this.remoteQuery = remoteQuery;
    }
}