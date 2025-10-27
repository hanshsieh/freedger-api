package org.freedger.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OXRCurrencyType {
  @JsonProperty("fiat")
  FIAT,
  @JsonProperty("crypto")
  CRYPTO,
  @JsonProperty("commodity")
  COMMODITY,
}
