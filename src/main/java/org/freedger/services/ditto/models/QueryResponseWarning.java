package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

/** Represents a warning in the query response from Ditto's API. */
public class QueryResponseWarning {
  @SerializedName("_id")
  private Object id;

  @SerializedName("description")
  private String description;

  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
