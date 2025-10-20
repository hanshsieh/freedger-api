package org.freedger.repository.ditto.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class GetLedgerRequest {
  @NotNull private String id;

  @NotNull private String userId;

  @Nullable private String transactionId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
}
