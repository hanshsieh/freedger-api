package org.freedger.domain.config;

import java.time.Duration;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class DittoConfig {
  @Config(path = "api-key-secret-name")
  private String apiKeySecretName;
  
  @Config(path = "provider-name")
  private String providerName;
  
  @Config(path = "token-expires-in")
  private Duration tokenExpiresIn;

  @Config(path = "app-id")
  private String appId;
  
  @Config(path = "api-base-url")
  private String apiBaseUrl;
}
