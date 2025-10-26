package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;

@Value
@Builder
@Jacksonized
public class Instrument {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  private int schemaVersion = SCHEMA_VERSION;

  @JsonProperty("_id")
  @NonNull
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
