package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LedgerChildId {
  @JsonProperty("id")
  private String id;

  @JsonProperty("ledgerId")
  private String ledgerId;

  public String getId() {
    return id;
  }

  public LedgerChildId setId(String id) {
    this.id = id;
    return this;
  }

  public String getLedgerId() {
    return ledgerId;
  }

  public LedgerChildId setLedgerId(String ledgerId) {
    this.ledgerId = ledgerId;
    return this;
  }
}
