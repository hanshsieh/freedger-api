package org.freedger.repository.ditto.models;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueryCurrenciesRequest {
  @Nullable
  private String transactionId;

  @Nullable
  private String ledgerId;

  @Nullable
  private CurrencyType type;

  @Nullable
  private String code;

  @Nullable
  private Boolean archived;

  @Nullable
  private Integer limit;

  @Nullable
  private Integer offset;
}
