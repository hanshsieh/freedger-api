package org.freedger.services.ditto.models;

import java.util.Map;
import java.time.Instant;
import com.google.gson.annotations.SerializedName;

import jakarta.validation.constraints.NotNull;

public class Account {
    public static final int SCHEMA_VERSION = 1;
    
    @SerializedName("schemaVersion")
    private int schemaVersion = SCHEMA_VERSION;

    @SerializedName("_id")
    @NotNull
    private String id;

    @SerializedName("createdAt")
    @NotNull
    private Instant createdAt;

    @SerializedName("updatedAt")
    @NotNull
    private Instant updatedAt;

    @SerializedName("type")
    @NotNull
    private String type;

    @SerializedName("name")
    @NotNull
    private String name;

    @SerializedName("isArchived")
    private boolean archived;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("currencyId")
    @NotNull
    private String currencyId;

    @SerializedName("isAutoClear")
    private boolean autoClear;

    @SerializedName("channels")
    @NotNull
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
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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
        return autoClear;
    }

    public void setAutoClear(boolean isAutoClear) {
        this.autoClear = isAutoClear;
    }

    public Map<String, AccountChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, AccountChannel> channels) {
        this.channels = channels;
    }

}
