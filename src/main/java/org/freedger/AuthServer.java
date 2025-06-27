package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Azure Functions with HTTP Trigger for Ditto Token Exchange.
 */
public class AuthServer {
    private static final Gson gson = new Gson();
    private static final String AUTH0_DOMAIN = System.getenv("AUTH0_DOMAIN");
    private static final String AUTH0_AUDIENCE = System.getenv("AUTH0_AUDIENCE");
    private static final String DITTO_APP_ID = System.getenv("DITTO_APP_ID");
    private static final String DITTO_TOKEN_SECRET = System.getenv("DITTO_TOKEN_SECRET");
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
        
        try {
            // 1. Validate request
            if (!request.getBody().isPresent()) {
                return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Request body is missing");
            }

            // 2. Parse request
            JsonObject requestBody = gson.fromJson(request.getBody().get(), JsonObject.class);
            if (!requestBody.has("token")) {
                return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Token is required");
            }
            String auth0Token = requestBody.get("token").getAsString();

            // 3. Validate Auth0 Token
            DecodedJWT jwt = validateAuth0Token(auth0Token);
            if (jwt == null) {
                return createErrorResponse(request, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }

            // 4. Generate Ditto Token
            String dittoToken = generateDittoToken(jwt.getSubject());

            // 5. Return response
            Map<String, String> result = new HashMap<>();
            result.put("token", dittoToken);
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(result))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private DecodedJWT validateAuth0Token(String token) {
        try {
            // 1. Decode JWT without verification
            DecodedJWT jwt = JWT.decode(token);
            
            // 2. Get JWK
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            
            // 3. Create verification algorithm with JWK
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            
            // 4. Verify token
            algorithm.verify(jwt);
            
            // 5. Validate audience
            if (AUTH0_AUDIENCE != null && !jwt.getAudience().contains(AUTH0_AUDIENCE)) {
                throw new RuntimeException("Invalid audience");
            }
            
            // 6. Validate issuer
            if (!jwt.getIssuer().equals(String.format("https://%s/", AUTH0_DOMAIN))) {
                throw new RuntimeException("Invalid issuer");
            }
            
            return jwt;
            
        } catch (Exception e) {
            return null;
        }
    }

    private String generateDittoToken(String userId) {
        // Set expiration time (1 hour from now)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600 * 1000);
        
        // Create Ditto Token
        return JWT.create()
                .withIssuer(DITTO_APP_ID)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("ditto_sync_docs", true)  // Enable document sync
                .withClaim("ditto_sync_write", true)  // Allow write operations
                .sign(Algorithm.HMAC256(DITTO_TOKEN_SECRET));
    }

    private HttpResponseMessage createErrorResponse(
            HttpRequestMessage<?> request, 
            HttpStatus status, 
            String message) {
        
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(gson.toJson(error))
                .build();
    }
}
