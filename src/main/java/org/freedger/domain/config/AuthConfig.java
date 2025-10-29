package org.freedger.domain.config;

import org.github.gestalt.config.annotations.Config;

import lombok.Getter;

@Getter
public class AuthConfig {
  @Config(path = "issuer")
  private String issuer;
  @Config(path = "audience")
  private String audience;
  @Config(path = "jwks")
  private String jwks;
}
