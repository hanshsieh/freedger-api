package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class Account {
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

  @JsonProperty("type")
  @NotNull
  private AccountType type;

  @JsonProperty("name")
  @NotNull
  private String name;

  @Nullable
  @JsonProperty("archivedAt")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant archivedAt;

  @JsonProperty("groupId")
  @Nullable
  private String groupId;

  @JsonProperty("currencyId")
  @NotNull
  private String currencyId;

  @JsonProperty("initialBalance")
  @NotNull
  private BigDecimal initialBalance;

  @JsonProperty("autoReconcile")
  private boolean autoReconcile = true;

  @JsonProperty("note")
  @NotNull
  private String note = "";

  @JsonProperty("order")
  private double order = 0.0;

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

  public AccountType getType() {
    return type;
  }

  public void setType(AccountType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Instant getArchivedAt() {
    return archivedAt;
  }

  public void setArchivedAt(Instant archivedAt) {
    this.archivedAt = archivedAt;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(String currencyId) {
    this.currencyId = currencyId;
  }

  public BigDecimal getInitialBalance() {
    return initialBalance;
  }

  public void setInitialBalance(BigDecimal openingBalance) {
    this.initialBalance = openingBalance;
  }

  public boolean isAutoReconcile() {
    return autoReconcile;
  }

  public void setAutoReconcile(boolean autoReconcile) {
    this.autoReconcile = autoReconcile;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public double getOrder() {
    return order;
  }

  public void setOrder(double order) {
    this.order = order;
  }
}
