package org.freedger.ditto.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the response from Ditto's "/store/execute" API.
 * Follows the schema defined in the Ditto API specification.
 */
public class QueryResponse<Item, ItemId> {
    @SerializedName("transactionId")
    private long transactionId;

    @SerializedName("queryType")
    private String queryType;

    @SerializedName("items")
    private List<Item> items;

    @SerializedName("mutatedDocumentIds")
    private List<ItemId> mutatedDocumentIds;

    @SerializedName("error")
    private QueryResponseError error;

    @SerializedName("warnings")
    private List<QueryResponseWarning> warnings;

    @SerializedName("totalWarningsCount")
    private long totalWarningsCount;

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<ItemId> getMutatedDocumentIds() {
        return mutatedDocumentIds;
    }

    public void setMutatedDocumentIds(List<ItemId> mutatedDocumentIds) {
        this.mutatedDocumentIds = mutatedDocumentIds;
    }

    public QueryResponseError getError() {
        return error;
    }

    public void setError(QueryResponseError error) {
        this.error = error;
    }

    public List<QueryResponseWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<QueryResponseWarning> warnings) {
        this.warnings = warnings;
    }

    public long getTotalWarningsCount() {
        return totalWarningsCount;
    }

    public void setTotalWarningsCount(long totalWarningsCount) {
        this.totalWarningsCount = totalWarningsCount;
    }
}
