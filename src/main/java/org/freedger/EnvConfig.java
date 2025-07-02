package org.freedger;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class EnvConfig implements Config {
    private final String authIssuer;
    private final String authAudience;
    private final String authJwks;
    private final String dittoAppId;
    private String dittoApiKey;
    private final String dittoApiBaseUrl;
    private final String dittoProviderName;
    private final int dittoTokenExpireSec;
    private final SecretClient secretClient;

    public EnvConfig() {
        this(createSecretClient());
    }

    public EnvConfig(SecretClient secretClient) {
        this.secretClient = secretClient;
        this.authIssuer = getRequiredEnv("AUTH_ISSUER");
        this.authAudience = getRequiredEnv("AUTH_AUDIENCE");
        this.authJwks = getRequiredEnv("AUTH_JWKS");
        this.dittoAppId = getRequiredEnv("DITTO_APP_ID");
        this.dittoApiBaseUrl = getRequiredEnv("DITTO_API_BASE_URL");
        
        String dittoApiKeyName = getRequiredEnv("DITTO_API_KEY_SECRET_NAME");
        this.dittoApiKey = this.secretClient.getSecret(dittoApiKeyName).getValue();
        this.dittoProviderName = getRequiredEnv("DITTO_PROVIDER_NAME");
        this.dittoTokenExpireSec = getRequiredIntEnv("DITTO_TOKEN_EXPIRE_SEC");
    }

    private static SecretClient createSecretClient() {
        String keyVaultUrl = getRequiredEnv("KEY_VAULT_URL");
        return new SecretClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
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
    public String dittoApiKey() {
        return this.dittoApiKey;
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
            throw new RuntimeException("Environment variable " + name + " must be an integer: " + value, e);
        }
    }
}
