package org.freedger.services.ditto.models;

import com.google.gson.annotations.SerializedName;

/** Represents an error in the query response from Ditto's API. */
public class QueryResponseError {
  @SerializedName("description")
  private String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
