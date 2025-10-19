package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class AccountChannel {
  @NotNull
  @SerializedName("name")
  private String name;

  @SerializedName("order")
  private double order;

  @Nullable
  @SerializedName("archivedAt")
  private Instant archivedAt;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getOrder() {
    return order;
  }

  public void setOrder(double order) {
    this.order = order;
  }

  public Instant getArchivedAt() {
    return archivedAt;
  }

  public void setArchivedAt(Instant archivedAt) {
    this.archivedAt = archivedAt;
  }
}
