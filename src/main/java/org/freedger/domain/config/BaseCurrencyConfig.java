package org.freedger.domain.config;

import lombok.Getter;

import org.github.gestalt.config.annotations.Config;

@Getter
public class BaseCurrencyConfig {
  @Config(path = "code")
  private String code;
  
  @Config(path = "name")
  private String name;

  @Config(path = "decimal-places")
  private int decimalPlaces;

  @Config(path = "type")
  private CurrencyType type;

  @Config(path = "enabled")
  private boolean enabled;
}
