package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;

@Value
@Builder
@Jacksonized
public class Currency {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  @Builder.Default
  private int schemaVersion = SCHEMA_VERSION;

  /**
   * The ID of the currency.
   * ID can be null when creating a currency with the legacy API.
   */
  @JsonProperty("_id")
  private LedgerChildId id;

  @JsonProperty("createdAt")
  @NonNull
  private Instant createdAt;

  @JsonProperty("updatedAt")
  @NonNull
  private Instant updatedAt;

  @JsonProperty("archivedAt")
  private Instant archivedAt;

  @JsonProperty("type")
  @NonNull
  private CurrencyType type;

  @JsonProperty("name")
  @NonNull
  private String name;

  @JsonProperty("code")
  @NonNull
  private String code;

  @JsonProperty("decimals")
  private int decimals;

  @JsonProperty("instrumentId")
  @NonNull
  private String instrumentId;
}
