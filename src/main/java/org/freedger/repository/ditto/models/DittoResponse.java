package org.freedger.repository.ditto.models;

public class DittoResponse<T> {
  private final String transactionId;
  private final T data;

  public DittoResponse(String transactionId, T data) {
    this.transactionId = transactionId;
    this.data = data;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public T getData() {
    return data;
  }
}
