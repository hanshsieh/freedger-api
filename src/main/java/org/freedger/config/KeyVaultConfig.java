package org.freedger.config;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class KeyVaultConfig {
  @Config(path = "url")
  private String url;
}
