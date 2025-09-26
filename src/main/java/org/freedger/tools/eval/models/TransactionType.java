package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransactionType {
  @JsonProperty("payment")
  PAYMENT,
  @JsonProperty("receive")
  RECEIVE,
  @JsonProperty("transfer")
  TRANSFER;

  public static TransactionType fromString(String type) {
    return TransactionType.valueOf(type.toUpperCase());
  }
}
