package org.freedger.repository.ditto.models;

import java.time.Instant;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class QueryQuotesRequest {
  @Nullable
  private String transactionId;

  @NotNull
  private String instrumentId;

  @Nullable
  private Instant timeBegin;

  @Nullable
  private Instant timeEnd;

  @Nullable
  private Integer limit;

  @Nullable
  private Integer offset;

  @NotNull
  private QuoteOrder order = QuoteOrder.TIME_DESC;

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

  public QuoteOrder getOrder() {
    return order;
  }

  public void setOrder(QuoteOrder order) {
    this.order = order;
  }
}
