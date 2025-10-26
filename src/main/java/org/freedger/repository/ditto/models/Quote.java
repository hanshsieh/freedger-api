package org.freedger.repository.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class Quote {
  public static final int SCHEMA_VERSION = 1;

  @JsonProperty("schemaVersion")
  private int schemaVersion = SCHEMA_VERSION;

  @JsonProperty("_id")
  @NotNull
  private LedgerChildId id;

  @JsonProperty("createdAt")
  @NotNull
  private Instant createdAt;

  @JsonProperty("updatedAt")
  @NotNull
  private Instant updatedAt;

  @JsonProperty("instrumentId")
  @NotNull
  private String instrumentId;

  @JsonProperty("time")
  @NotNull
  private Instant time;

  @JsonProperty("value")
  @NotNull
  private BigDecimal value;

  @Nullable
  @JsonProperty("source") 
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String source;

  public int getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(int schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public LedgerChildId getId() {
    return id;
  }

  public void setId(LedgerChildId id) {
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

  public String getInstrumentId() {
    return instrumentId;
  }

  public void setInstrumentId(String instrumentId) {
    this.instrumentId = instrumentId;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
