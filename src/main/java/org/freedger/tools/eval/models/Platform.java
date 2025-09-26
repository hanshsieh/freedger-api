package org.freedger.tools.eval.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Platform {
  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("items")
  public List<PlatformItem> items;
}
