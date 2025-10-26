package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;
import jakarta.annotation.Nullable;

@Value
@Builder
@Jacksonized
public class Instrument {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  @Builder.Default
  private int schemaVersion = SCHEMA_VERSION;

  /**
   * The ID of the instrument.
   * ID can be null when creating an instrument with the legacy API.
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

  @JsonProperty("symbol")
  @NonNull
  private String symbol;

  @JsonProperty("name")
  @NonNull
  private String name;

  @JsonProperty("category")
  @NonNull
  private InstrumentCategory category;

  @JsonProperty("decimals")
  private int decimals;

  @JsonProperty("quoteCurrencyId")
  @NonNull
  private String quoteCurrencyId;

  @JsonProperty("initialQuote")
  @NonNull
  private BigDecimal initialQuote;
}
