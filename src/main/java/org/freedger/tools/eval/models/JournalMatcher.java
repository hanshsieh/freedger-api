package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public class JournalMatcher {
  @Schema(name = "accountId", description = "Expected account ID of the journal.")
  @JsonProperty("accountId")
  public String accountId;

  @Schema(
      name = "accountChannelId",
      description = "Expected account channel ID of the journal.",
      nullable = true)
  @JsonProperty("accountChannelId")
  public String accountChannelId;

  @Schema(
      name = "platformId",
      description = "Expected platform ID of the journal.",
      nullable = true)
  @JsonProperty("platformId")
  public String platformId;

  @Schema(name = "amount", description = "Expected amount of the journal.")
  @JsonProperty("amount")
  public String amount;

  @Schema(name = "currencyId", description = "Expected currency ID of the journal.")
  @JsonProperty("currencyId")
  public String currencyId;

  @Schema(name = "time", description = "Expected time of the journal.")
  @JsonProperty("time")
  public String time;

  @Schema(name = "affectsBalance", description = "Expected affectsBalance of the journal.")
  @JsonProperty("affectsBalance")
  public boolean affectsBalance;
}
