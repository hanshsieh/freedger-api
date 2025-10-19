package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
