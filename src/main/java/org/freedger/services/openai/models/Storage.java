package org.freedger.services.openai.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Storage {
  @JsonProperty("currencies")
  public List<Currency> currencies;

  @JsonProperty("accounts")
  public List<Account> accounts;

  @JsonProperty("categories")
  public List<Category> categories;

  @JsonProperty("platforms")
  public List<Platform> platforms;
}
