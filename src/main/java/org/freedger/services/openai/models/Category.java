package org.freedger.services.openai.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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