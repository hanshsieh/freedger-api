package org.freedger.di.module;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import dagger.Module;
import dagger.Provides;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

import org.freedger.config.AppConfig;
import org.freedger.config.Config;
import org.freedger.config.EnvConfig;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.repository.openexchangerates.OpenExchangeRatesClient;
import org.freedger.service.AuthService;
import org.freedger.service.HttpMessageSerializer;
import org.freedger.service.LedgerService;
import org.freedger.service.QuoteUpdater;
import org.freedger.service.RequestValidator;
import org.freedger.service.TokenValidator;

@Module
public class AppModule {
  @Provides
  @Singleton
  public DittoClient provideDittoHttpClient(Config config, SecretClient secretClient) {
    String apiKey = secretClient.getSecret(config.dittoApiKeySecretName()).getValue();
    return new DittoClient(config.dittoApiBaseUrl(), apiKey);
  }

  @Provides
  @Singleton
  public JwkProvider provideJwkProvider(Config config) {
    try {
      return new JwkProviderBuilder(new URI(config.authJwks()).toURL())
          .cached(10, 24, TimeUnit.HOURS)
          .timeouts(5000, 5000)
          .build();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException("Invalid JWKS URL: " + config.authJwks(), e);
    }
  }

  @Provides
  @Singleton
  public LedgerService provideLedgerService(DittoClient dittoClient) {
    return new LedgerService(dittoClient);
  }

  @Provides
  @Singleton
  public AuthService provideAuthService(Config config, DittoClient dittoClient) {
    return new AuthService(config, dittoClient);
  }

  @Provides
  @Singleton
  public OpenExchangeRatesClient provideOpenExchangeRatesClient(Config config, SecretClient secretClient) {
    String appId = secretClient.getSecret(config.openExchangeRatesAppIdSecretName()).getValue();
    return new OpenExchangeRatesClient(appId);
  }

  @Provides
  @Singleton
  public QuoteUpdater provideQuoteUpdater(
    AppConfig config, 
    OpenExchangeRatesClient exchangeRatesClient, 
    DittoClient dittoClient) {
    return new QuoteUpdater(config, exchangeRatesClient, dittoClient);
  }

  @Provides
  @Singleton
  public Config provideConfig() {
    return new EnvConfig();
  }

  @Provides
  @Singleton
  public AppConfig provideAppConfig() {
    try {
      return AppConfig.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load app config", e);
    }
  }

  @Provides
  @Singleton
  public TokenCredential provideTokenCredential() {
    return new DefaultAzureCredentialBuilder().build();
  }

  @Provides
  @Singleton
  public SecretClient provideSecretClient(Config config, TokenCredential credential) {
    String keyVaultUrl = config.keyVaultUrl();
    return new SecretClientBuilder().vaultUrl(keyVaultUrl).credential(credential).buildClient();
  }

  @Provides
  @Singleton
  public RequestValidator provideRequestValidator() {
    return new RequestValidator();
  }

  @Provides
  @Singleton
  public TokenValidator provideTokenValidator(JwkProvider authProviderJwks, Config config) {
    return new TokenValidator(authProviderJwks, config);
  }

  @Provides
  @Singleton
  public HttpMessageSerializer provideHttpMessageSerializer() {
    return new HttpMessageSerializer();
  }
}
