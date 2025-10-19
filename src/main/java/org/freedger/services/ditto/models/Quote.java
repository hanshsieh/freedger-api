package org.freedger.services.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class Quote {
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

  @SerializedName("instrumentId")
  @NotNull
  private String instrumentId;

  @SerializedName("time")
  @NotNull
  private Instant time;

  @SerializedName("value")
  @NotNull
  private BigDecimal value;

  @Nullable
  @SerializedName("source") 
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
