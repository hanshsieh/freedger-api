package org.freedger.services.ditto.models;

import java.time.Instant;
import com.google.gson.annotations.SerializedName;

import jakarta.validation.constraints.NotNull;

public class Account {
    public static final int SCHEMA_VERSION = 1;
    
    @SerializedName("schemaVersion")
    private int schemaVersion = SCHEMA_VERSION;

    @SerializedName("_id")
    @NotNull
    private LedgerChildId id;

    @SerializedName("createdAt")
    @NotNull
    private Instant createdAt;

    @SerializedName("updatedAt")
    @NotNull
    private Instant updatedAt;

    @SerializedName("type")
    @NotNull
    private AccountType type;

    @SerializedName("name")
    @NotNull
    private String name;

    @SerializedName("isArchived")
    private boolean archived = false;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("currencyId")
    @NotNull
    private String currencyId;

    @SerializedName("isAutoClear")
    private boolean autoClear = true;

    @SerializedName("order")
    private double order = 1.0;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public LedgerChildId getId() {
        return id;
    }

    public void setId(LedgerChildId id) {
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

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
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

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
    }
}
