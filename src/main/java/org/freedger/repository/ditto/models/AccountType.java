package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountType {
  @JsonProperty("cash")
  CASH,
  @JsonProperty("loadable")
  LOADABLE,
  @JsonProperty("bank")
  BANK,
  @JsonProperty("volatile")
  VOLATILE,
  @JsonProperty("credit")
  CREDIT,
  @JsonProperty("counterparty")
  COUNTERPARTY,
  @JsonProperty("loan")
  LOAN,
  @JsonProperty("invoice")
  INVOICE,
}
