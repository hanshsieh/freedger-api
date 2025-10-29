package org.freedger.domain.config;

import java.util.List;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class OpenExchangeRatesConfig {
  @Config(path = "appIdSecretName")
  private String appIdSecretName;

  @Config(path = "maxUpdateDays")
  private int maxUpdateDays = 20;
  
  @Config(path = "quoteCurrency")
  private String quoteCurrency = "USD";
  
  @Config(path = "baseCurrencies")
  private List<BaseCurrencyConfig> baseCurrencies;
}
