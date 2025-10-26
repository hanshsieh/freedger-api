package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LedgerChildId {
  @JsonProperty("id")
  private String id;

  @JsonProperty("ledgerId")
  // Ditto auth webhook uses legacy DQL, which doesn't support
  // querying docs with a missing field.
  @JsonInclude(JsonInclude.Include.ALWAYS)
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
