package org.freedger.services.ditto.models;

import java.util.Map;
import java.time.Instant;
import com.google.gson.annotations.SerializedName;

public class Account {
    public static final int SCHEMA_VERSION = 1;
    
    @SerializedName("schemaVersion")
    private int schemaVersion = SCHEMA_VERSION;

    @SerializedName("_id")
    private String id;

    @SerializedName("createdAt")
    private Instant createdAt;

    @SerializedName("updatedAt")
    private Instant updatedAt;

    @SerializedName("type")
    private String type;

    @SerializedName("name")
    private String name;

    @SerializedName("isArchived")
    private boolean isArchived;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("currencyId")
    private String currencyId;

    @SerializedName("isAutoClear")
    private boolean isAutoClear;

    @SerializedName("channels")
    private Map<String, AccountChannel> channels;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public boolean isAutoClear() {
        return isAutoClear;
    }

    public void setAutoClear(boolean isAutoClear) {
        this.isAutoClear = isAutoClear;
    }

    public Map<String, AccountChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, AccountChannel> channels) {
        this.channels = channels;
    }

}
