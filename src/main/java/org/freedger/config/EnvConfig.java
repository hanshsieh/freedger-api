package org.freedger.config;

public class EnvConfig implements Config {
  private final String keyVaultUrl;
  private final String authIssuer;
  private final String authAudience;
  private final String authJwks;
  private final String dittoAppId;
  private final String dittoApiBaseUrl;
  private final String dittoProviderName;
  private final int dittoTokenExpireSec;
  private final String dittoApiKeySecretName;
  private final String openExchangeRatesAppIdSecretName;
  private final String openExchangeRatesConfigPath;

  public EnvConfig() {
    this.keyVaultUrl = getRequiredEnv("KEY_VAULT_URL");
    this.authIssuer = getRequiredEnv("AUTH_ISSUER");
    this.authAudience = getRequiredEnv("AUTH_AUDIENCE");
    this.authJwks = getRequiredEnv("AUTH_JWKS");
    this.dittoAppId = getRequiredEnv("DITTO_APP_ID");
    this.dittoApiBaseUrl = getRequiredEnv("DITTO_API_BASE_URL");
    this.dittoApiKeySecretName = getRequiredEnv("DITTO_API_KEY_SECRET_NAME");
    this.dittoProviderName = getRequiredEnv("DITTO_PROVIDER_NAME");
    this.dittoTokenExpireSec = getRequiredIntEnv("DITTO_TOKEN_EXPIRE_SEC");
    this.openExchangeRatesAppIdSecretName = getRequiredEnv("OPEN_EXCHANGE_RATES_APP_ID_SECRET_NAME");
    this.openExchangeRatesConfigPath = getRequiredEnv("OPEN_EXCHANGE_RATES_CONFIG_PATH");
  }

  @Override
  public String keyVaultUrl() {
    return this.keyVaultUrl;
  }

  @Override
  public String authIssuer() {
    return this.authIssuer;
  }

  @Override
  public String authAudience() {
    return this.authAudience;
  }

  @Override
  public String authJwks() {
    return this.authJwks;
  }

  @Override
  public String dittoAppId() {
    return this.dittoAppId;
  }

  @Override
  public String dittoApiBaseUrl() {
    return this.dittoApiBaseUrl;
  }

  @Override
  public String dittoProvider() {
    return this.dittoProviderName;
  }

  @Override
  public int dittoTokenExpireSec() {
    return this.dittoTokenExpireSec;
  }

  @Override
  public String dittoApiKeySecretName() {
    return this.dittoApiKeySecretName;
  }

  @Override
  public String openExchangeRatesAppIdSecretName() {
    return this.openExchangeRatesAppIdSecretName;
  }

  @Override
  public String openExchangeRatesConfigPath() {
    return this.openExchangeRatesConfigPath;
  }

  private static String getRequiredEnv(String name) {
    String value = System.getenv(name);
    if (value == null) {
      throw new IllegalArgumentException("Environment variable " + name + " cannot be null");
    }
    return value;
  }

  private static int getRequiredIntEnv(String name) {
    String value = getRequiredEnv(name);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new RuntimeException(
          "Environment variable " + name + " must be an integer: " + value, e);
    }
  }
}
