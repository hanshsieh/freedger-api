package org.freedger;

public interface Config {
    /**
     * The issuer of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com
     * 
     * @return The issuer of the authentication provider.
     */
    String authProviderIssuer();

    /**
     * The audience of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com
     * 
     * @return The audience of the authentication provider.
     */
    String authProviderAudience();

    /**
     * The JWKS endpoint of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com/.well-known/jwks.json
     * 
     * @return The JWKS endpoint of the authentication provider.
     */
    String authProviderJwks();

    /**
     * The Ditto App ID for authentication.
     * 
     * @return The Ditto App ID
     */
    String dittoAppId();

    /**
     * The Ditto API Key for authentication.
     * 
     * @return The Ditto API Key
     */
    String dittoApiKey();

    /**
     * The Ditto API Base URL for authentication.
     * 
     * @return The Ditto API Base URL
     */
    String dittoApiBaseUrl();

    /**
     * The provider name for Ditto webhook authentication.
     * 
     * @return The provider name
     */
    String dittoProviderName();

    /**
     * The expiration time of the Ditto token in seconds.
     * 
     * @return The expiration time of the token in seconds
     */
    int dittoTokenExpireSec();
}
