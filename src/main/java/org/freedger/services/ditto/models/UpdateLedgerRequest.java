package org.freedger.services.ditto.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateLedgerRequest {
  @NotNull private String id;

  @NotNull private String userId;

  @Nullable private String transactionId;

  @NotNull private String name;

  @NotNull private String note;

  @NotNull private String currencyId;

  @NotNull private List<String> writerIds;

  @NotNull private List<String> readerIds;

  @NotNull private String externalAccountId;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(String currencyId) {
    this.currencyId = currencyId;
  }

  public List<String> getWriterIds() {
    return writerIds;
  }

  public void setWriterIds(List<String> writerIds) {
    this.writerIds = writerIds;
  }

  public List<String> getReaderIds() {
    return readerIds;
  }

  public void setReaderIds(List<String> readerIds) {
    this.readerIds = readerIds;
  }

  public String getExternalAccountId() {
    return externalAccountId;
  }

  public void setExternalAccountId(String externalAccountId) {
    this.externalAccountId = externalAccountId;
  }
}
