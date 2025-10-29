package org.freedger.config;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class OpenExchangeRatesConfig {
  @Config(path = "appIdSecretName")
  private String appIdSecretName;
  
  @Config(path = "configPath")
  private String configPath;
}
