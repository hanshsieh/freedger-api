package org.freedger.repository.ditto.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateCurrencyResponse {
  @NonNull
  private String currencyId;
  @NonNull
  private String instrumentId;
}
