package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.freedger.model.TokenExchangeRequest;
import org.freedger.model.TokenExchangeResponse;
import org.freedger.util.JsonUtils;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Azure Functions with HTTP Trigger for Ditto Token Exchange.
 */
public class AuthServer {
    private static final String CLAIM_DITTO_READ = "ditto_read";
    private static final String CLAIM_DITTO_WRITE = "ditto_write";
    private static final String AUTH0_DOMAIN = System.getenv("AUTH0_DOMAIN");
    private static final String AUTH0_AUDIENCE = System.getenv("AUTH0_AUDIENCE");
    private static final String DITTO_APP_ID = System.getenv("DITTO_APP_ID");
    private static final String EXCHANGE_TOKEN_SECRET = System.getenv("EXCHANGE_TOKEN_SECRET");
    private static final JwkProvider jwkProvider = new JwkProviderBuilder(
            String.format("https://%s/", AUTH0_DOMAIN))
            .cached(10, 24, TimeUnit.HOURS)
            .build();

    @FunctionName("CreateDittoExchangeToken")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        return JsonUtils.processRequest(
            request,
            TokenExchangeRequest.class,
            tokenRequest -> {
                // Validate token is not empty
                if (tokenRequest.getToken() == null || tokenRequest.getToken().trim().isEmpty()) {
                    throw new IllegalArgumentException("Token is required");
                }
                
                // Validate Auth0 Token
                DecodedJWT jwt = validateAuth0Token(tokenRequest.getToken());
                
                // Generate Ditto Token
                String dittoToken = generateExchangeToken(jwt.getSubject());
                
                // Return response
                return new TokenExchangeResponse(dittoToken);
            }
        );
    }

    /**
     * Validates an Auth0 JWT token.
     * @param token The JWT token to validate
     * @return Decoded and verified JWT
     * @throws SecurityException if the token is invalid or verification fails
     */
    private static DecodedJWT validateAuth0Token(String token) throws SecurityException {
        // 1. Decode JWT without verification
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(token);
            
            // 2. Get JWK
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            
            // 3. Create verification algorithm with JWK
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            
            // 4. Verify token
            algorithm.verify(jwt);
        } catch (Exception ex) {
            throw new SecurityException("Invalid token: " + ex.getMessage());
        }
        
        // 5. Validate audience
        if (AUTH0_AUDIENCE != null && !jwt.getAudience().contains(AUTH0_AUDIENCE)) {
            throw new SecurityException("Invalid audience. Expected: " + AUTH0_AUDIENCE);
        }
        
        // 6. Validate issuer
        String expectedIssuer = String.format("https://%s/", AUTH0_DOMAIN);
        if (!jwt.getIssuer().equals(expectedIssuer)) {
            throw new SecurityException("Invalid issuer. Expected: " + expectedIssuer);
        }
        
        return jwt;
    }

    private static String generateExchangeToken(String userId) {
        // Set expiration time (1 hour from now)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600 * 1000);
        
        // Create Exchange Token
        return JWT.create()
                .withIssuer(DITTO_APP_ID)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim(CLAIM_DITTO_READ, true)  // Enable read operations
                .withClaim(CLAIM_DITTO_WRITE, true)  // Allow write operations
                .sign(Algorithm.HMAC256(EXCHANGE_TOKEN_SECRET));
    }

    // Error response creation is now handled by JsonUtils
}
