package org.freedger.ditto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response from Ditto's query API.
 */
public class DittoQueryResponse<T> {
    @JsonProperty("transactionId")
    private long transactionId;

    @JsonProperty("items")
    private List<T> items;

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
