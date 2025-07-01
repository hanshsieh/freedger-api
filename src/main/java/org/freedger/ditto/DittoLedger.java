package org.freedger.ditto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a Ledger document from Ditto.
 */
public class DittoLedger {
    public static final String COLLECTION = "Ledgers";

    @SerializedName("_id")
    private String id;
    
    @SerializedName("readerIds")
    private List<String> readerIds;
    
    @SerializedName("writerIds")
    private List<String> writerIds;

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
}
