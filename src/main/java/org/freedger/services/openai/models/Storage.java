package org.freedger.services.openai.models;

import java.util.List;

import org.freedger.services.eval.models.Currency;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.freedger.services.eval.models.Account;
import org.freedger.services.eval.models.Category;
import org.freedger.services.eval.models.Platform;

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
