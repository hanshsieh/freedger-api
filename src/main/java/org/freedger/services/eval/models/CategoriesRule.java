package org.freedger.services.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class CategoriesRule {
  @Schema(description = "Type of the validation rule.", allowableValues = {"categories"})
  public String type;

  @Schema(name = "categoryIds", description = "Expected category IDs of the transaction.")
  public List<String> categoryIds;
}
