package org.freedger.module;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.freedger.config.Config;
import org.freedger.config.EnvConfig;
import org.freedger.ditto.DittoHttpClient;
import org.freedger.function.AppValidator;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    @Provides
    @Singleton
    public DittoHttpClient provideDittoHttpClient(Config config, SecretClient secretClient) {
        String apiKey = secretClient.getSecret(config.dittoApiKeySecretName()).getValue();
        return new DittoHttpClient(config.dittoApiBaseUrl(), apiKey);
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
    public Config provideConfig() {
        return new EnvConfig();
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
        return new SecretClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(credential)
            .buildClient();
    }

    @Provides
    @Singleton
    public AppValidator provideAppValidator() {
        return new AppValidator();
    }
}
