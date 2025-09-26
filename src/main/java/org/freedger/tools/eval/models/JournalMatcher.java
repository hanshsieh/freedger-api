package org.freedger.tools.eval.models;

import io.swagger.v3.oas.annotations.media.Schema;

public class JournalMatcher {
  @Schema(name = "accountId", description = "Expected account ID of the journal.")
  public String accountId;

  @Schema(name = "accountChannelId", description = "Expected account channel ID of the journal.", nullable = true)
  public String accountChannelId;

  @Schema(name = "platformId", description = "Expected platform ID of the journal.", nullable = true)
  public String platformId;

  @Schema(name = "amount", description = "Expected amount of the journal.")
  public String amount;

  @Schema(name = "currencyId", description = "Expected currency ID of the journal.")
  public String currencyId;

  @Schema(name = "time", description = "Expected time of the journal.")
  public String time;

  @Schema(name = "inBalance", description = "Expected inBalance of the journal.")
  public boolean inBalance;
}
