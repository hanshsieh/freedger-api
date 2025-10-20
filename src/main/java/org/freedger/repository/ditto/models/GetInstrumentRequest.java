package org.freedger.repository.ditto.models;

import com.google.gson.annotations.SerializedName;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class GetInstrumentRequest {
  @Nullable
  @SerializedName("transactionId")
  private String transactionId;

  @NotNull
  @SerializedName("ledgerId")
  private String ledgerId;

  @NotNull
  @SerializedName("instrumentId")
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
