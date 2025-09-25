package org.freedger.services.eval.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.freedger.services.openai.models.AccountCategory;
import org.freedger.services.openai.models.AccountChannel;

public class Account {
  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("groupName")
  public String groupName;

  @JsonProperty("type")
  public AccountType type;

  @JsonProperty("category")
  public AccountCategory category;

  @JsonProperty("currencyId")
  public String currencyId;

  @JsonProperty("channels")
  public List<AccountChannel> channels;
}
