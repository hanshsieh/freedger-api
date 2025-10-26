package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WriteCommandResult {
  @JsonProperty("method")
  private String method;

  @JsonProperty("transactionId")
  private long transactionId;

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(long transactionId) {
    this.transactionId = transactionId;
  }
}
