package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class AccountChannel {
  @NotNull
  @JsonProperty("name")
  private String name;

  @JsonProperty("order")
  private double order;

  @Nullable
  @JsonProperty("archivedAt")
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
