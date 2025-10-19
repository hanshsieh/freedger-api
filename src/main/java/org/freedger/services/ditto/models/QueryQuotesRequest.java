package org.freedger.services.ditto.models;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class QueryQuotesRequest {
  @Nullable
  @SerializedName("transactionId")
  private String transactionId;

  @NotNull
  @SerializedName("instrumentId")
  private String instrumentId;

  @Nullable
  @SerializedName("timeBegin")
  private Instant timeBegin;

  @Nullable
  @SerializedName("timeEnd")
  private Instant timeEnd;

  @Nullable
  @SerializedName("limit")
  private Integer limit;

  @Nullable
  @SerializedName("offset")
  private Integer offset;

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getInstrumentId() {
    return instrumentId;
  }

  public void setInstrumentId(String instrumentId) {
    this.instrumentId = instrumentId;
  }

  public Instant getTimeBegin() {
    return timeBegin;
  }

  public void setTimeBegin(Instant timeBegin) {
    this.timeBegin = timeBegin;
  }

  public Instant getTimeEnd() {
    return timeEnd;
  }

  public void setTimeEnd(Instant timeEnd) {
    this.timeEnd = timeEnd;
  }

  public Integer getLimit() {
    return limit;
  }
  
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }
}
