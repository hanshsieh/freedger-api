package org.freedger.domain.config;

import java.util.List;

import lombok.Getter;

import org.github.gestalt.config.annotations.Config;

@Getter
public class QuoteConfig {
  @Config(path = "maxUpdateDays")
  private int maxUpdateDays = 20;
  
  @Config(path = "quoteCurrency")
  private String quoteCurrency = "USD";
  
  @Config(path = "baseCurrencies")
  private List<BaseCurrencyConfig> baseCurrencies;
}
