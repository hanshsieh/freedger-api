package org.freedger.repository.ditto.models;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateLedgerRequest {
  @NotNull private String name;

  @NotNull private String note;

  @NotNull private String currencyId;

  @NotNull private List<String> writerIds;

  @NotNull private List<String> readerIds;

  @NotNull private String externalAccountName;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getExternalAccountName() {
    return externalAccountName;
  }

  public void setExternalAccountName(String externalAccountName) {
    this.externalAccountName = externalAccountName;
  }
}
