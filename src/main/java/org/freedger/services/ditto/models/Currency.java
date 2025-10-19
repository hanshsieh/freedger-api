package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class Currency {
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

  @SerializedName("archivedAt")
  @Nullable
  private Instant archivedAt;

  @SerializedName("type")
  @NotNull
  private CurrencyType type;

  @SerializedName("name")
  @NotNull
  private String name;

  @SerializedName("code")
  @NotNull
  private String code;

  @SerializedName("decimals")
  private int decimals;

  @SerializedName("instrumentId")
  @NotNull
  private String instrumentId;
}
