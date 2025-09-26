package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class TypeRule {
  @Schema(name = "type", description = "Type of the validation rule.", allowableValues = {"type"})
  @JsonProperty("type")
  public String type;

  @Schema(name = "transactionType", description = "Expected type of the transaction.")
  @JsonProperty("transactionType")
  public String transactionType;
}
