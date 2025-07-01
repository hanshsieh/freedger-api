package org.freedger;

public class EnvConfig implements Config {
    public static final EnvConfig instance = new EnvConfig();

    private final String authIssuer;
    private final String authAudience;
    private final String authJwks;
    private final String dittoAppId;
    private final String dittoApiKey;
    private final String dittoApiBaseUrl;
    private final String dittoProviderName;
    private final int dittoTokenExpireSec;

    public EnvConfig() {
        this.authIssuer = getRequiredEnv("AUTH_ISSUER");
        this.authAudience = getRequiredEnv("AUTH_AUDIENCE");
        this.authJwks = getRequiredEnv("AUTH_JWKS");
        this.dittoAppId = getRequiredEnv("DITTO_APP_ID");
        this.dittoApiKey = getRequiredEnv("DITTO_API_KEY");
        this.dittoApiBaseUrl = getRequiredEnv("DITTO_API_BASE_URL");
        this.dittoProviderName = getRequiredEnv("DITTO_PROVIDER_NAME");
        this.dittoTokenExpireSec = getRequiredIntEnv("DITTO_TOKEN_EXPIRE_SEC");
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

    private String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable " + name + " cannot be null");
        }
        return value;
    }

    private int getRequiredIntEnv(String name) {
        String value = getRequiredEnv(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Environment variable " + name + " must be an integer");
        }
    }
}
