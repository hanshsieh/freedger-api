package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateCurrencyRequest {
  @Nullable
  private String transactionId;

  @Nullable
  private String ledgerId;

  @NonNull
  private CurrencyType type;

  @NonNull
  private String code;

  @NonNull
  private String name;
  @NonNull
  private String symbol;
  private int decimals;
  @NonNull
  private InstrumentCategory category;

  @Nullable
  private Instant archivedAt;

  @NonNull
  String quoteCurrencyId;

  @NonNull
  private BigDecimal initialQuote;
}
