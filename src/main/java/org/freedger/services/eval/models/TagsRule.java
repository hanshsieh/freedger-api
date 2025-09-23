package org.freedger.services.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class TagsRule extends ValidationRule {
  public TagsRule() {
    this.type = "tags";
  }

  @Schema(name = "tags", description = "Expected tags of the transaction.")
  public List<String> tags;
}
