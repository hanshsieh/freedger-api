package org.freedger.services.ditto.models;

import jakarta.annotation.Nullable;

public class QueryCurrenciesRequest {
  @Nullable
  private String transactionId;

  @Nullable
  private String ledgerId;

  @Nullable
  private CurrencyType type;

  @Nullable
  private String code;

  @Nullable
  private Boolean archived;

  @Nullable
  private String userId;

  @Nullable
  private Integer limit;

  @Nullable
  private Integer offset;

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

  public CurrencyType getType() {
    return type;
  }

  public void setType(CurrencyType type) {
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Boolean getArchived() {
    return archived;
  }

  public void setArchived(Boolean archived) {
    this.archived = archived;
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

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
