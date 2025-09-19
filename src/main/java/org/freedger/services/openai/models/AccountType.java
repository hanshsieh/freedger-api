package org.freedger.services.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountType {
  @JsonProperty("cash")
  CASH(AccountCategory.PERSONAL),

  @JsonProperty("loadable")
  LOADABLE(AccountCategory.PERSONAL),

  @JsonProperty("bank")
  BANK(AccountCategory.PERSONAL),

  @JsonProperty("volatile")
  VOLATILE(AccountCategory.PERSONAL),

  @JsonProperty("credit")
  CREDIT(AccountCategory.PERSONAL),

  @JsonProperty("loan")
  LOAN(AccountCategory.PERSONAL),

  @JsonProperty("counterparty")
  COUNTERPARTY(AccountCategory.EXTERNAL);

  public final AccountCategory category;

  AccountType(AccountCategory category) {
    this.category = category;
  }

  public static AccountType fromString(String type) {
    return AccountType.valueOf(type.toUpperCase());
  }
}
