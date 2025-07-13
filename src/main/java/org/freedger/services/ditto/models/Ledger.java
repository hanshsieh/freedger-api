package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.time.Instant;

/**
 * Represents a Ledger document from Ditto.
 */
public class Ledger {
    public static final int SCHEMA_VERSION = 1;
    
    @SerializedName("_id")
    private String id;
    
    @SerializedName("schemaVersion")
    private int schemaVersion = SCHEMA_VERSION;

    @SerializedName("createdAt")
    private Instant createdAt;

    @SerializedName("updatedAt")
    private Instant updatedAt;

    @SerializedName("name")
    private String name;
    
    @SerializedName("readerIds")
    private List<String> readerIds;
    
    @SerializedName("writerIds")
    private List<String> writerIds;

    @SerializedName("note")
    private String note;

    @SerializedName("externalAccountId")
    private String externalAccountId;

    @SerializedName("currencyId")
    private String currencyId;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getReaderIds() {
        return readerIds;
    }

    public void setReaderIds(List<String> readerIds) {
        this.readerIds = readerIds;
    }

    public List<String> getWriterIds() {
        return writerIds;
    }

    public void setWriterIds(List<String> writerIds) {
        this.writerIds = writerIds;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }
}
