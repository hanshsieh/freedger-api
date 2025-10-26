package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;

@Value
@Builder
@Jacksonized
public class LedgerChildId {
  @JsonProperty("id")
  @NonNull
  private String id;

  @JsonProperty("ledgerId")
  // Ditto auth webhook uses legacy DQL, which doesn't support
  // querying docs with a missing field.
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @Nullable
  private String ledgerId;
}
