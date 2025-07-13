package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public class WriteCommandResult {
    @SerializedName("method")
    private String method;

    @SerializedName("transactionId")
    private String transactionId;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
