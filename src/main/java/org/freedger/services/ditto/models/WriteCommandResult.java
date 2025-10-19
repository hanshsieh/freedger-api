package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

public class WriteCommandResult {
  @SerializedName("method")
  private String method;

  @SerializedName("transactionId")
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
