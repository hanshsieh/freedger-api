package org.freedger.function.utils;

import java.security.interfaces.RSAPublicKey;
import java.util.regex.Pattern;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwk.JwkProvider;
import org.freedger.config.Config;
import com.microsoft.azure.functions.HttpRequestMessage;

public class TokenValidator {
    private final JwkProvider authProviderJwks;
    private final Config config;
    private static final Pattern AUTH_HEADER_PATTERN = 
        Pattern.compile("^Bearer\s+(\\S+)$", Pattern.CASE_INSENSITIVE);

    public TokenValidator(JwkProvider authProviderJwks, Config config) {
        this.authProviderJwks = authProviderJwks;
        this.config = config;
    }

    public DecodedJWT validate(HttpRequestMessage<?> request, ScopePredicate scopePredicate) throws SecurityException {
        final var authHeader = request.getHeaders().get("authorization");
        if (authHeader == null) {
            throw new SecurityException("Missing Authorization header");
        }
        final var matcher = AUTH_HEADER_PATTERN.matcher(authHeader);
        if (!matcher.matches()) {
            throw new SecurityException("Invalid Authorization header");
        }
        var token = matcher.group(1);
        return validate(token, scopePredicate);
    }

    public DecodedJWT validate(String token, ScopePredicate scopePredicate) throws SecurityException {
        try {
            // Verify token signature and basic claims
            DecodedJWT jwt = JWT.decode(token);

            // Get the key from the JWKS endpoint
            Jwk jwk = authProviderJwks.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            // Verify the token
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(config.authIssuer())
                    .withAudience(config.authAudience())
                    .withClaim(ScopePredicate.CLAIM_NAME, scopePredicate)
                    .acceptLeeway(10)
                    .build();

            return verifier.verify(token);
        } catch (JWTVerificationException | JwkException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }
    }
}
