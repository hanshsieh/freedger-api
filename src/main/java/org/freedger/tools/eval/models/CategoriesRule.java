package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class CategoriesRule {
  @Schema(
      name = "type",
      description = "Type of the validation rule.",
      allowableValues = {"categories"})
  @JsonProperty("type")
  public String type;

  @Schema(name = "categoryIds", description = "Expected category IDs of the transaction.")
  @JsonProperty("categoryIds")
  public List<String> categoryIds;
}
