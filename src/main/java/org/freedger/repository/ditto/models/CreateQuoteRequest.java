package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateQuoteRequest {
  @Nullable
  private String transactionId;

  @NonNull
  private String ledgerId;

  @NonNull
  private String instrumentId;

  @NonNull
  private Instant time;

  @NonNull
  private BigDecimal value;

  @Nullable
  private String source;
}
