package org.freedger.services.eval.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.freedger.services.openai.models.TransactionType;
import org.freedger.services.openai.models.Journal;

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
