package org.freedger.service.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OXRConfig {
  @JsonProperty("baseCurrency")
  @NonNull
  String baseCurrency;

  @JsonProperty("quoteCurrencies")
  @NonNull
  List<OXRCurrency> quoteCurrencies;
}


