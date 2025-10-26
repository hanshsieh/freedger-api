package org.freedger.repository.ditto.models;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.NonNull;

/** Represents a Ledger document from Ditto. */
@Value
@Builder
@Jacksonized
public class Ledger {
  public static final int SCHEMA_VERSION = 1;

  /**
   * The ID of the ledger.
   * ID can be null when creating a ledger with the legacy API.
   */
  @JsonProperty("_id")
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;

  @JsonProperty("schemaVersion")
  @Builder.Default
  private int schemaVersion = SCHEMA_VERSION;

  @JsonProperty("createdAt")
  @NonNull
  private Instant createdAt;

  @JsonProperty("updatedAt")
  @NonNull
  private Instant updatedAt;

  @JsonProperty("name")
  @NonNull
  private String name;

  @JsonProperty("readerIds")
  @NonNull
  @Builder.Default
  private List<String> readerIds = Collections.emptyList();

  @JsonProperty("writerIds")
  @NonNull
  @Builder.Default
  private List<String> writerIds = Collections.emptyList();

  @JsonProperty("note")
  @NonNull
  @Builder.Default
  private String note = "";

  @JsonProperty("externalAccountId")
  @NonNull
  private String externalAccountId;

  @JsonProperty("currencyId")
  @NonNull
  private String currencyId;

  public org.freedger.domain.models.Ledger toDomain() {
    return org.freedger.domain.models.Ledger.builder()
        .id(id)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .name(name)
        .readerIds(readerIds)
        .writerIds(writerIds)
        .note(note)
        .externalAccountId(externalAccountId)
        .currencyId(currencyId)
        .build();
  }
}
