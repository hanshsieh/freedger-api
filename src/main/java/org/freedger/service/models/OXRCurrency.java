package org.freedger.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OXRCurrency {
  @JsonProperty("code")
  @NonNull
  String code;

  @JsonProperty("name")
  @NonNull
  String name;

  @JsonProperty("decimalPlaces")
  int decimalPlaces;

  @JsonProperty("type")
  @NonNull
  OXRCurrencyType type;

  @JsonProperty("enabled")
  boolean enabled;
}


