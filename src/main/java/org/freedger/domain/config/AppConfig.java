package org.freedger.domain.config;

import java.io.IOException;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.hocon.HoconLoader;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;

import lombok.Getter;

@Getter
public class AppConfig {
  private static final String ENV_NAME = "ENV";

  @Config(path = "keyVault")
  private KeyVaultConfig keyVault;

  @Config(path = "auth")
  private AuthConfig auth;

  @Config(path = "ditto")
  private DittoConfig ditto;

  @Config(path = "openExchangeRates")
  private OpenExchangeRatesConfig openExchangeRates;

  @Config(path = "quotesUpdateDays")
  private int quotesUpdateDays = 0;

  public static AppConfig load() throws IOException {
    String env = System.getenv(ENV_NAME);
    if (env == null) {
      throw new IllegalArgumentException("Missing environment variable " + ENV_NAME);
    }
    return load(env);
  }

  public static AppConfig load(String env) throws IOException {
    try {
      Gestalt gestalt = new GestaltBuilder()
        .addSource(ClassPathConfigSourceBuilder.builder()
          .setResource(env + ".conf")
          .build())
        .addConfigLoader(new HoconLoader())
        .build();
      gestalt.loadConfigs();
      return gestalt.getConfig("", AppConfig.class);
    } catch (Exception e) {
      throw new IOException("Failed to load config for environment " + env, e);
    }
  }
}
