package org.freedger.services.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Transaction {
  @JsonProperty("type")
  public TransactionType type;

  @JsonProperty("credits")
  public List<Journal> credits;

  @JsonProperty("debits")
  public List<Journal> debits;

  @JsonProperty("tags")
  public List<String> tags;

  @JsonProperty("note")
  public String note;
}
