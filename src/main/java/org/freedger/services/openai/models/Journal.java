package org.freedger.services.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Journal {
  @JsonProperty("accountId")
  @Schema(description = "Account ID.")
  public String accountId;
  
  @JsonProperty("accountChannelId")
  @Schema(description = "Account channel ID.", nullable = true)
  public String accountChannelId;
  
  @JsonProperty("platformId")
  @Schema(description = "Platform ID.", nullable = true)
  public String platformId;
  
  @JsonProperty("amount")
  @Schema(description = "Amount of the journal, such as \"100.12\"")
  public String amount;
  
  @JsonProperty("currencyId")
  @Schema(description = "Currency ID of the amount.")
  public String currencyId;
  
  @JsonProperty("time")
  @Schema(description = "Time of the journal.", format = "date-time")
  public String time;
  
  @JsonProperty("inBalance")
  @Schema(description = "Whether the amount is included in balance of the account.")
  public boolean inBalance;
}
