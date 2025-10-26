package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class GetInstrumentRequest {
  @Nullable
  private String transactionId;

  @NonNull
  private String ledgerId;

  @NonNull
  private String instrumentId;
}
