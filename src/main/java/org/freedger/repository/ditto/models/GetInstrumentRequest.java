package org.freedger.repository.ditto.models;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class GetInstrumentRequest {
  @Nullable
  private String transactionId;

  @Nullable
  private String ledgerId;

  @NonNull
  private String instrumentId;
}
