package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwk.JwkException;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.freedger.dto.ErrorResponse;
import org.freedger.dto.TokenExchangeRequest;
import org.freedger.dto.TokenExchangeResponse;

/**
 * Azure Functions with HTTP Trigger for Ditto Token Exchange.
 */
public class AuthServer {
    private static final String CLAIM_DITTO_READ = "ditto_read";
    private static final String CLAIM_DITTO_WRITE = "ditto_write";
    private final String auth0Domain;
    private final String auth0Audience;
    private final String dittoAppId;
    private final String exchangeTokenSecret;
    private final JwkProvider jwkProvider;

    /**
     * Constructor for production use that creates a real JwkProvider.
     */
    public AuthServer() {
        this(
            System.getenv("AUTH0_DOMAIN"),
            System.getenv("AUTH0_AUDIENCE"),
            System.getenv("DITTO_APP_ID"),
            System.getenv("EXCHANGE_TOKEN_SECRET"),
            new JwkProviderBuilder(String.format("https://%s/", System.getenv("AUTH0_DOMAIN")))
                .cached(10, 24, TimeUnit.HOURS)
                .build()
        );
    }

    /**
     * Constructor for testing that allows injecting dependencies.
     */
    protected AuthServer(String auth0Domain, String auth0Audience, String dittoAppId, 
                        String exchangeTokenSecret, JwkProvider jwkProvider) {
        this.auth0Domain = auth0Domain;
        this.auth0Audience = auth0Audience;
        this.dittoAppId = dittoAppId;
        this.exchangeTokenSecret = exchangeTokenSecret;
        this.jwkProvider = jwkProvider;
    }

    @FunctionName("CreateDittoExchangeToken")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<TokenExchangeRequest> request,
            final ExecutionContext context) {
        
        try {
            // Get request body (already deserialized to TokenExchangeRequest by Azure Functions)
            TokenExchangeRequest tokenRequest = request.getBody();
            
            // Validate request
            if (tokenRequest == null || tokenRequest.getToken() == null || tokenRequest.getToken().isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Token is required")
                    .build();
            }
            
            // Validate Auth0 token
            DecodedJWT jwt = validateAuth0Token(tokenRequest.getToken());
            
            // Generate Ditto exchange token
            String exchangeToken = generateExchangeToken(jwt.getSubject());
            
            // Return response
            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(new TokenExchangeResponse(exchangeToken))
                .build();
                
        } catch (SecurityException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid token: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"))
                .build();
        }
    }

    /**
     * Validates the Auth0 JWT token and returns the decoded JWT.
     * @param token The JWT token to validate
     * @return The decoded JWT
     * @throws SecurityException if the token is invalid
     */
    private DecodedJWT validateAuth0Token(String token) throws SecurityException {
        try {
            // Verify token signature and basic claims
            DecodedJWT jwt = JWT.decode(token);
            
            // Get the key from the JWKS endpoint
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            
            // Verify the token
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(String.format("https://%s/", auth0Domain))
                .withAudience(auth0Audience)
                .acceptLeeway(1)
                .build();
                
            return verifier.verify(token);
        } catch (JWTVerificationException | JwkException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SecurityException("Error validating token: " + e.getMessage(), e);
        }
    }

    private String generateExchangeToken(String subject) {
        // Set expiration time (1 hour from now)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600 * 1000);
        
        // Create Exchange Token
        try {
            return JWT.create()
                .withIssuer(dittoAppId)
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim(CLAIM_DITTO_READ, true)
                .withClaim(CLAIM_DITTO_WRITE, true)
                .sign(Algorithm.HMAC256(exchangeTokenSecret));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate exchange token", e);
        }
    }

    // Error response creation is now handled by JsonUtils
}
