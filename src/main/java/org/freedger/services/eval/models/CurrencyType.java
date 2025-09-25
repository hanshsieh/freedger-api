package org.freedger.services.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CurrencyType {
  @JsonProperty("fiat")
  FIAT,
  @JsonProperty("crypto")
  CRYPTO,
  @JsonProperty("other")
  OTHER,
}