package org.freedger.domain.models;


import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Result<T> {
  @NonNull
  private final T data;

  @NonNull
  private final String transactionId;
}
