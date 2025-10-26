package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class GetInstrumentRequest {
  @Nullable
  @JsonProperty("transactionId")
  private String transactionId;

  @NotNull
  @JsonProperty("ledgerId")
  private String ledgerId;

  @NotNull
  @JsonProperty("instrumentId")
  private String instrumentId;

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getLedgerId() {
    return ledgerId;
  }

  public void setLedgerId(String ledgerId) {
    this.ledgerId = ledgerId;
  }

  public String getInstrumentId() {
    return instrumentId;
  }

  public void setInstrumentId(String instrumentId) {
    this.instrumentId = instrumentId;
  }
}
