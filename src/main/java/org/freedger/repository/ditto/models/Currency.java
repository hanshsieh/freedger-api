package org.freedger.repository.ditto.models;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class Currency {
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

  @SerializedName("archivedAt")
  private Instant archivedAt;

  @SerializedName("type")
  @NonNull
  private CurrencyType type;

  @SerializedName("name")
  @NonNull
  private String name;

  @SerializedName("code")
  @NonNull
  private String code;

  @SerializedName("decimals")
  private int decimals;

  @SerializedName("instrumentId")
  @NonNull
  private String instrumentId;
}
