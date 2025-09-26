package org.freedger.tools.eval.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class JournalsRule {
  @Schema(name = "type", description = "Type of the validation rule.", allowableValues = {"journals"})
  @JsonProperty("type")
  public String type;

  @Schema(name = "journalType", description = "Type of the journals.", allowableValues = {"credit", "debit"})
  @JsonProperty("journalType")
  public String journalType;

  @Schema(name = "journals", description = "Matchers for the journals. All the journals must match exactly one of the matchers (regardless of order).")
  @JsonProperty("journals")
  public List<JournalMatcher> journals;
}
