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
    // The issuer of the Auth0 JWT token, such as "https://auth0.com/"
    private final String authProviderIssuer;
    // The audience of the Auth0 JWT token, such as "https://auth0.com/"
    private final String authProviderAudience;
    private final String tokenIssuer;
    private final String tokenSecret;
    private final JwkProvider authProviderJwks;

    public AuthServer() {
        this(
            Env.authProviderIssuer(),
            Env.authProviderAudience(),
            new JwkProviderBuilder(Env.authProviderJwks())
                .cached(10, 24, TimeUnit.HOURS)
                .build(),
            Env.tokenIssuer(),
            Env.tokenSecret()
        );
    }

    protected AuthServer(
        String authProviderIssuer, 
        String authProviderAudience, 
        JwkProvider authProviderJwks,
        String tokenIssuer, 
        String exchangeTokenSecret) {
        this.authProviderIssuer = authProviderIssuer;
        this.authProviderAudience = authProviderAudience;
        this.authProviderJwks = authProviderJwks;
        this.tokenIssuer = tokenIssuer;
        this.tokenSecret = exchangeTokenSecret;
    }

    @FunctionName("CreateDittoExchangeToken")
    public HttpResponseMessage createDittoExchangeToken(
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
            if (tokenRequest == null || tokenRequest.getToken() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Token is required"))
                    .build();
            }
            
            // Validate Auth0 token
            DecodedJWT jwt = validateAuthProviderToken(tokenRequest.getToken());
            
            // Generate Ditto exchange token
            String exchangeToken = generateExchangeToken(jwt.getSubject());
            
            // Return response
            return request.createResponseBuilder(HttpStatus.OK)
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
     * Validates the auth provider JWT token and returns the decoded JWT.
     * @param token The JWT token to validate
     * @return The decoded JWT
     * @throws SecurityException if the token is invalid
     */
    private DecodedJWT validateAuthProviderToken(String token) throws SecurityException {
        try {
            // Verify token signature and basic claims
            DecodedJWT jwt = JWT.decode(token);
            
            // Get the key from the JWKS endpoint
            Jwk jwk = authProviderJwks.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            
            // Verify the token
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(authProviderIssuer)
                .withAudience(authProviderAudience)
                .acceptLeeway(10)
                .build();
                
            return verifier.verify(token);
        } catch (JWTVerificationException | JwkException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }
    }

    private String generateExchangeToken(String subject) {
        // Set expiration time (1 hour from now)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 60 * 60 * 1000);
        
        // Create Exchange Token
        try {
            return JWT.create()
                .withIssuer(tokenIssuer)
                .withAudience(tokenIssuer)
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim(CLAIM_DITTO_READ, true)
                .withClaim(CLAIM_DITTO_WRITE, true)
                .sign(Algorithm.HMAC256(tokenSecret));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate exchange token", e);
        }
    }

    // Error response creation is now handled by JsonUtils
}
