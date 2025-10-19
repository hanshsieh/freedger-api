package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Platform {
  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("items")
  public List<PlatformItem> items;
}
