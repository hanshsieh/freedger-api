package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public class LedgerChildId {
    @SerializedName("id")
    private String id;
    
    @SerializedName("ledgerId")
    private String ledgerId;

    public String getId() {
        return id;
    }

    public LedgerChildId setId(String id) {
        this.id = id;
        return this;
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public LedgerChildId setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
        return this;
    }


}
