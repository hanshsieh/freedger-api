package org.freedger.services.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountChannel {
  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;
}
