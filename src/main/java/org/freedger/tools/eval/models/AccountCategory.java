package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountCategory {
  @JsonProperty("personal")
  PERSONAL,

  @JsonProperty("external")
  EXTERNAL;

  public static AccountCategory fromString(String category) {
    return AccountCategory.valueOf(category.toUpperCase());
  }
}
