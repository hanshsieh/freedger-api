package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

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
public class Quote {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  @Builder.Default
  private int schemaVersion = SCHEMA_VERSION;

  /**
   * The ID of the quote.
   * ID can be null when creating a quote with the legacy API.
   */
  @JsonProperty("_id")
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LedgerChildId id;

  @JsonProperty("createdAt")
  @NonNull
  private Instant createdAt;

  @JsonProperty("updatedAt")
  @NonNull
  private Instant updatedAt;

  @JsonProperty("instrumentId")
  @NonNull
  private String instrumentId;

  @JsonProperty("time")
  @NonNull
  private Instant time;

  @JsonProperty("value")
  @NonNull
  private BigDecimal value;

  @Nullable
  @JsonProperty("source") 
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String source;

}
