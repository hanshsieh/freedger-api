package org.freedger.domain.config;

import java.util.List;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class OpenExchangeRatesConfig {
  @Config(path = "app-id-secret-name")
  private String appIdSecretName;

  @Config(path = "max-update-days")
  private int maxUpdateDays = 20;
  
  @Config(path = "quote-currency")
  private String quoteCurrency = "USD";
  
  @Config(path = "base-currencies")
  private List<BaseCurrencyConfig> baseCurrencies;
}
