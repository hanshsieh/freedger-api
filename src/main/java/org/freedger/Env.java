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
     * The audience of the JWT token.
     * E.g. https://freedger.org
     * 
     * @return The audience of the JWT token.
     */
    public static String tokenAudience() {
        return System.getenv("TOKEN_AUDIENCE");
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
     * The first secret key for generating the JWT token.
     * When creating a new token, always use the first secret key.
     * 
     * @return The first secret key for generating the JWT token.
     */
    public static String tokenSecret1() {
        return System.getenv("TOKEN_SECRET1");
    }

    /**
     * The second secret key for generating the JWT token.
     * When rotating the secret key, rotate the first one, reload the function app, then rotate the second one.
     * 
     * @return The second secret key for generating the JWT token.
     */
    public static String tokenSecret2() {
        return System.getenv("TOKEN_SECRET2");
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
