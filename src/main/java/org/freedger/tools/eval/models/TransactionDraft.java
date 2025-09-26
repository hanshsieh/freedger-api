package org.freedger.tools.eval.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TransactionDraft", description = "The transaction draft allowing the user to review and give feedback.")
public class TransactionDraft {
  @Schema(description = "Type of the transaction.", allowableValues = {"payment", "receive", "transfer"})
  public String type;

  @Schema(description = "The credit journals of the transaction.")
  public List<Journal> credits;

  @Schema(description = "The debit journals of the transaction.")
  public List<Journal> debits;

  @Schema(description = "The category IDs of the transaction.")
  public List<String> categoryIds;

  @Schema(description = "The tags of the transaction.")
  public List<String> tags;

  @Schema(description = "Additional note for the transaction. Can be empty.")
  public String note;

  public Object execute() {
    return null;
  }
}
