package org.freedger.tools.eval.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Category {
  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("groupName")
  public String groupName;

  @JsonProperty("transactionType")
  public TransactionType transactionType;

  @JsonProperty("tags")
  public List<String> tags;
}
