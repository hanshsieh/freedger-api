package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a Ledger document from Ditto.
 */
public class Ledger {
    public static final String COLLECTION = "Ledgers";

    @SerializedName("_id")
    private String id;
    
    @SerializedName("readerIds")
    private List<String> readerIds;
    
    @SerializedName("writerIds")
    private List<String> writerIds;

    @SerializedName("note")
    private String note;

    @SerializedName("currencyId")
    private String currencyId;

    @SerializedName("externalAccountId")
    private String externalAccountId;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
