package org.freedger.tools.eval.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EvalContext {
  @JsonProperty("currentTime")
  public String currentTime;

  @JsonProperty("timeZone")
  public String timeZone;

  @JsonProperty("locale")
  public String locale;

  @JsonProperty("defaultExternalAccountId")
  public String defaultExternalAccountId;

  @JsonProperty("defaultCurrencyId")
  public String defaultCurrencyId;

  @JsonProperty("evalId")
  public String evalId;

  @JsonProperty("evalRunName")
  public String evalRunName;

  @JsonProperty("model")
  public String model;

  @JsonProperty("reasoningEffort")
  public String reasoningEffort;

  @JsonProperty("currencies")
  public List<Currency> currencies;

  @JsonProperty("accounts")
  public List<Account> accounts;

  @JsonProperty("categories")
  public List<Category> categories;

  @JsonProperty("platforms")
  public List<Platform> platforms;

  @JsonProperty("tags")
  public List<String> tags;
}
