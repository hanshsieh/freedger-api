package org.freedger;

public class Env {
    /**
     * The issuer of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com
     * 
     * @return The issuer of the authentication provider.
     */
    public static String authProviderIssuer() {
        return System.getenv("AUTH_PROVIDER_ISSUER");
    }

    /**
     * The audience of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com
     * 
     * @return The audience of the authentication provider.
     */
    public static String authProviderAudience() {
        return System.getenv("AUTH_PROVIDER_AUDIENCE");
    }

    /**
     * The issuer of the JWT token.
     * E.g. https://freedger.org
     * 
     * @return The issuer of the JWT token.
     */
    public static String tokenIssuer() {
        return System.getenv("TOKEN_ISSUER");
    }

    /**
     * The public key for verifying the JWT token.
     * 
     * @return The public key for verifying the JWT token.
     */
    public static String tokenPublicKey() {
        return System.getenv("TOKEN_PUBLIC_KEY");
    }

    /**
     * The secret key for generating the JWT token.
     * 
     * @return The secret key for generating the JWT token.
     */
    public static String tokenSecret() {
        return System.getenv("TOKEN_SECRET");
    }

    /**
     * The JWKS endpoint of the authentication provider.
     * E.g. https://yourtenant.jp.auth0.com/.well-known/jwks.json
     * 
     * @return The JWKS endpoint of the authentication provider.
     */
    public static String authProviderJwks() {
        return System.getenv("AUTH_PROVIDER_JWKS");
    }
}
