package org.freedger.domain.config;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class OpenExchangeRatesConfig {
  @Config(path = "appIdSecretName")
  private String appIdSecretName;
}
