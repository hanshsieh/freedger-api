package org.freedger.repository.ditto.models;


import java.time.Instant;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

/** Represents a Ledger document from Ditto. */
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
  private int schemaVersion = SCHEMA_VERSION;

  @JsonProperty("createdAt")
  @NotNull
  private Instant createdAt;

  @JsonProperty("updatedAt")
  @NotNull
  private Instant updatedAt;

  @JsonProperty("name")
  @NotNull
  private String name;

  @JsonProperty("readerIds")
  @NotNull
  private List<String> readerIds = Collections.emptyList();

  @JsonProperty("writerIds")
  @NotNull
  private List<String> writerIds = Collections.emptyList();

  @JsonProperty("note")
  @NotNull
  private String note = "";

  @JsonProperty("externalAccountId")
  @NotNull
  private String externalAccountId;

  @JsonProperty("currencyId")
  @NotNull
  private String currencyId;

  public int getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(int schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getReaderIds() {
    return readerIds;
  }

  public void setReaderIds(List<String> readerIds) {
    this.readerIds = readerIds;
  }

  public List<String> getWriterIds() {
    return writerIds;
  }

  public void setWriterIds(List<String> writerIds) {
    this.writerIds = writerIds;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(String currencyId) {
    this.currencyId = currencyId;
  }

  public String getExternalAccountId() {
    return externalAccountId;
  }

  public void setExternalAccountId(String externalAccountId) {
    this.externalAccountId = externalAccountId;
  }

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
