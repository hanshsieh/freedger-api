package org.freedger.repository.ditto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an error in the query response from Ditto's API. */
public class QueryResponseError {
  @JsonProperty("description")
  private String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
