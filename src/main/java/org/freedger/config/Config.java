package org.freedger.config;

public interface Config {

  /**
   * The Key Vault URL.
   *
   * @return The Key Vault URL
   */
  String keyVaultUrl();

  /**
   * The issuer of the authentication provider. E.g. https://yourtenant.jp.auth0.com
   *
   * @return The issuer of the authentication provider.
   */
  String authIssuer();

  /**
   * The audience of the authentication provider. E.g. https://yourtenant.jp.auth0.com
   *
   * @return The audience of the authentication provider.
   */
  String authAudience();

  /**
   * The JWKS endpoint of the authentication provider. E.g.
   * https://yourtenant.jp.auth0.com/.well-known/jwks.json
   *
   * @return The JWKS endpoint of the authentication provider.
   */
  String authJwks();

  /**
   * The Ditto App ID for authentication.
   *
   * @return The Ditto App ID
   */
  String dittoAppId();

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
  String dittoProvider();

  /**
   * The expiration time of the Ditto token in seconds.
   *
   * @return The expiration time of the token in seconds
   */
  int dittoTokenExpireSec();

  /**
   * The name of the secret in Key Vault that contains the Ditto API Key.
   *
   * @return The name of the secret in Key Vault
   */
  String dittoApiKeySecretName();
}
