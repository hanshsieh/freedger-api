package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public class UpsertCommand<Value> extends WriteCommand {
    @SerializedName("id")
    private String id;

    @SerializedName("ledgerId")
    private String ledgerId;

    @SerializedName("value")
    private Value value;

    public UpsertCommand() {
        super("upsert");
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLedgerId() {
        return ledgerId;
    }
    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }
    public Value getValue() {
        return value;
    }
    public void setValue(Value value) {
        this.value = value;
    }
}
