package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;

@Value
@Builder
@Jacksonized
public class Account {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  @Builder.Default
  private int schemaVersion = SCHEMA_VERSION;

  /**
   * The ID of the account.
   * ID can be null when creating an account with the legacy API.
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

  @JsonProperty("type")
  @NonNull
  private AccountType type;

  @JsonProperty("name")
  @NonNull
  private String name;

  @Nullable
  @JsonProperty("archivedAt")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant archivedAt;

  @Nullable
  @JsonProperty("groupId")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String groupId;

  @JsonProperty("currencyId")
  @NonNull
  private String currencyId;

  @JsonProperty("initialBalance")
  @NonNull
  private BigDecimal initialBalance;

  @JsonProperty("autoReconcile")
  @Builder.Default
  private boolean autoReconcile = true;

  @JsonProperty("note")
  @NonNull
  @Builder.Default
  private String note = "";

  @JsonProperty("order")
  @Builder.Default
  private double order = 0.0;

}
