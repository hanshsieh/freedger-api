package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InstrumentCategory {
  @JsonProperty("stocks")
  STOCKS,
  @JsonProperty("funds")
  FUNDS,
  @JsonProperty("futures")
  FUTURES,
  @JsonProperty("forex")
  FOREX,
  @JsonProperty("crypto")
  CRYPTO,
  @JsonProperty("indices")
  INDICES,
  @JsonProperty("bonds")
  BONDS,
  @JsonProperty("economy")
  ECONOMY,
  @JsonProperty("options")
  OPTIONS,
  @JsonProperty("other")
  OTHER,
}
