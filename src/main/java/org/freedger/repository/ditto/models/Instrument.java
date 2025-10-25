package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.google.gson.annotations.SerializedName;

import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class Instrument {
  public static final int SCHEMA_VERSION = 1;

  @SerializedName("schemaVersion")
  private int schemaVersion = SCHEMA_VERSION;

  @SerializedName("_id")
  @NonNull
  private LedgerChildId id;

  @SerializedName("createdAt")
  @NonNull
  private Instant createdAt;

  @SerializedName("updatedAt")
  @NonNull
  private Instant updatedAt;

  @SerializedName("symbol")
  @NonNull
  private String symbol;

  @SerializedName("name")
  @NonNull
  private String name;

  @SerializedName("category")
  @NonNull
  private InstrumentCategory category;

  @SerializedName("decimals")
  private int decimals;

  @SerializedName("quoteCurrencyId")
  @NonNull
  private String quoteCurrencyId;

  @SerializedName("initialQuote")
  @NonNull
  private BigDecimal initialQuote;
}
