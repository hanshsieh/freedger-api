package org.freedger.repository.ditto.models;

import java.time.Instant;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class QueryQuotesRequest {
  @Nullable
  private String transactionId;

  @NonNull
  private String instrumentId;

  @Nullable
  private Instant timeBegin;

  @Nullable
  private Instant timeEnd;

  @Nullable
  private Integer limit;

  @Nullable
  private Integer offset;

  @NonNull
  @Builder.Default
  private QuoteOrder order = QuoteOrder.TIME_DESC;
}
