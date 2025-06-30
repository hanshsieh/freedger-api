package org.freedger;

public class EnvConfig implements Config {
    public static final EnvConfig instance = new EnvConfig();

    @Override
    public String authProviderIssuer() {
        return getNonNullEnv("AUTH_PROVIDER_ISSUER");
    }

    @Override
    public String authProviderAudience() {
        return getNonNullEnv("AUTH_PROVIDER_AUDIENCE");
    }

    @Override
    public String authProviderJwks() {
        return getNonNullEnv("AUTH_PROVIDER_JWKS");
    }

    @Override
    public String dittoAppId() {
        return getNonNullEnv("DITTO_APP_ID");
    }
    
    @Override
    public String dittoApiKey() {
        return getNonNullEnv("DITTO_API_KEY");
    }
    
    @Override
    public String dittoApiBaseUrl() {
        return getNonNullEnv("DITTO_API_BASE_URL");
    }
    
    @Override
    public String dittoProviderName() {
        return getNonNullEnv("DITTO_PROVIDER_NAME");
    }

    @Override
    public int dittoTokenExpireSec() {
        return Integer.parseInt(getNonNullEnv("DITTO_TOKEN_EXPIRE_SEC"));
    }

    private String getNonNullEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable " + name + " cannot be null");
        }
        return value;
    }
}
