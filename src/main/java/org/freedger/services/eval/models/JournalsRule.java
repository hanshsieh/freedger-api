package org.freedger.services.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class JournalsRule {
  @Schema(description = "Type of the validation rule.", allowableValues = {"journals"})
  public String type;

  @Schema(name = "journalType", description = "Type of the journals.", allowableValues = {"credit", "debit"})
  public String journalType;

  @Schema(name = "journals", description = "Matchers for the journals. All the journals must match exactly one of the matchers (regardless of order).")
  public List<JournalMatcher> journals;
}
