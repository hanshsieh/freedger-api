package org.freedger.services.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class CategoriesRule extends ValidationRule {
  public CategoriesRule() {
    this.type = "categories";
  }

  @Schema(name = "categoryIds", description = "Expected category IDs of the transaction.")
  public List<String> categoryIds;
}
