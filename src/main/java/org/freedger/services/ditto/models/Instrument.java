package org.freedger.services.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.google.gson.annotations.SerializedName;

import jakarta.validation.constraints.NotNull;

public class Instrument {
  public static final int SCHEMA_VERSION = 1;

  @SerializedName("schemaVersion")
  private int schemaVersion = SCHEMA_VERSION;

  @SerializedName("_id")
  @NotNull
  private LedgerChildId id;

  @SerializedName("createdAt")
  @NotNull
  private Instant createdAt;

  @SerializedName("updatedAt")
  @NotNull
  private Instant updatedAt;

  @SerializedName("symbol")
  @NotNull
  private String symbol;

  @SerializedName("name")
  @NotNull
  private String name;

  @SerializedName("category")
  @NotNull
  private InstrumentCategory category;

  @SerializedName("decimals")
  @NotNull
  private int decimals;

  @SerializedName("quoteCurrencyId")
  @NotNull
  private String quoteCurrencyId;

  @SerializedName("initialQuote")
  @NotNull
  private BigDecimal initialQuote;
}
