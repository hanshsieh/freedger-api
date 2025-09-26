package org.freedger.tools.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

public class TypeRule {
  @Schema(description = "Type of the validation rule.", allowableValues = {"type"})
  public String type;

  @Schema(name = "transactionType", description = "Expected type of the transaction.")
  public String transactionType;
}
