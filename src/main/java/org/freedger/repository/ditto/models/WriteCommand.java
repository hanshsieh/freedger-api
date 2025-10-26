package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public abstract class WriteCommand {
  @JsonProperty("method")
  @NotNull
  private String method;

  @JsonProperty("collection")
  @NotNull
  private String collection;

  public WriteCommand(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }

  public String getCollection() {
    return collection;
  }

  public WriteCommand setCollection(String collection) {
    this.collection = collection;
    return this;
  }
}
