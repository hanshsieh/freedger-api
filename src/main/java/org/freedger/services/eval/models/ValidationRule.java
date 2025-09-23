package org.freedger.services.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class ValidationRule {
  @Schema(description = "Type of the validation rule.", allowableValues = {"categories", "tags"})
  public String type;
}
