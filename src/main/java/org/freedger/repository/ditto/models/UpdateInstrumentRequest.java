package org.freedger.repository.ditto.models;

import lombok.Builder;
import lombok.Value;
import lombok.NonNull;
import java.math.BigDecimal;

import jakarta.annotation.Nullable;

@Value
@Builder
public class UpdateInstrumentRequest {
  @Nullable
  private String transactionId;

  @Nullable
  private String ledgerId;

  @NonNull
  private String instrumentId;

  @NonNull
  private String symbol;

  @NonNull
  private String name;

  @NonNull
  private InstrumentCategory category;

  private int decimals;

  @NonNull
  private String quoteCurrencyId;

  @NonNull
  private BigDecimal initialQuote;
}
