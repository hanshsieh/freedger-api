package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotNull;

public abstract class WriteCommand {
  @SerializedName("method")
  @NotNull
  private String method;

  @SerializedName("collection")
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
