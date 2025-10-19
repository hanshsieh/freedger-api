package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class Account {
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

  @SerializedName("type")
  @NotNull
  private AccountType type;

  @SerializedName("name")
  @NotNull
  private String name;

  @SerializedName("archivedAt")
  @Nullable
  private Instant archivedAt;

  @SerializedName("groupId")
  @Nullable
  private String groupId;

  @SerializedName("currencyId")
  @NotNull
  private String currencyId;

  @SerializedName("initialBalance")
  @NotNull
  private BigDecimal initialBalance;

  @SerializedName("autoReconcile")
  private boolean autoReconcile = true;

  @SerializedName("note")
  @NotNull
  private String note = "";

  @SerializedName("order")
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
