package org.freedger.services.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Currency {
  @JsonProperty("id")
  public String id;

  @JsonProperty("type")
  public CurrencyType type;

  @JsonProperty("code")
  public String code;

  @JsonProperty("name")
  public String name;

  @JsonProperty("custom")
  public boolean custom;
}
