package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a warning in the query response from Ditto's API. */
public class QueryResponseWarning {
  @JsonProperty("_id")
  private Object id;

  @JsonProperty("description")
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
