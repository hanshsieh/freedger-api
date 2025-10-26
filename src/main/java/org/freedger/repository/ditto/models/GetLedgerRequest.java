package org.freedger.repository.ditto.models;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class GetLedgerRequest {
  @NonNull
  private String id;

  @NonNull
  private String userId;

  @Nullable
  private String transactionId;
}
