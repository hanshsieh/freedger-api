package org.freedger.domain.config;

import java.time.Duration;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class DittoConfig {
  @Config(path = "apiKeySecretName")
  private String apiKeySecretName;
  
  @Config(path = "providerName")
  private String providerName;
  
  @Config(path = "tokenExpiresIn")
  private Duration tokenExpiresIn;

  @Config(path = "appId")
  private String appId;
  
  @Config(path = "apiBaseUrl")
  private String apiBaseUrl;
}
