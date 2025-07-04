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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.freedger.ditto.DittoHttpClient;
import org.freedger.ditto.DittoLedger;
import org.freedger.openapi.model.AuthorizeRequest;
import org.freedger.openapi.model.AuthorizeResponse;
import org.freedger.openapi.model.Permission;
import org.freedger.openapi.model.PermissionRules;

/**
 * Azure Functions with HTTP Trigger for Ditto APIs.
 */
public class DittoApi {
    private final DittoHttpClient dittoClient;
    private final JwkProvider authProviderJwks;
    private final Config config;
    private final ScopePredicate scopePredicate;

    @Inject
    public DittoApi(
        Config config, 
        JwkProvider jwkProvider,
        DittoHttpClient dittoClient) {
        this.config = config;
        this.authProviderJwks = jwkProvider;
        this.dittoClient = dittoClient;
        this.scopePredicate = new ScopePredicate(new String[] { Scope.READ_DITTO_AUTH.getValue() });
    }

    /**
     * Handles the Ditto webhook authentication request.
     * Validates the request and returns the user's permissions.
     * 
     * @param request The incoming HTTP request
     * @param context The execution context
     * @return HTTP response with the user's permissions or an error message
     */
    @FunctionName("DittoAuthorize")
    public HttpResponseMessage dittoAuthorize(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "ditto/authorize") 
            HttpRequestMessage<AuthorizeRequest> request,
            final ExecutionContext context) {
        
        try {
            // Validate request
            validateRequest(context, request);

            // Get request body
            AuthorizeRequest webhookRequest = request.getBody();
            
            // Validate JWT token with specific audience and scope
            DecodedJWT jwt = validateToken(webhookRequest.getToken());
            String userId = jwt.getSubject();
            
            if (userId == null) {
                throw new SecurityException("Invalid token: missing subject");
            }
            
            List<DittoLedger> accessibleLedgers = dittoClient.findAccessibleLedgers(userId);

            AuthorizeResponse response = buildAuthResponse(userId, accessibleLedgers);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .build();
        } catch (IllegalArgumentException e) {
            context.getLogger().fine("Invalid request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(new AuthorizeResponse().authenticated(false))
                .build();
        } catch (SecurityException e) {
            context.getLogger().warning("Token validation failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(new AuthorizeResponse().authenticated(false))
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing Ditto permissions request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthorizeResponse().authenticated(false))
                .build();
        }
    }

    private void validateRequest(ExecutionContext context, HttpRequestMessage<AuthorizeRequest> request) {
        AuthorizeRequest webhookRequest = request.getBody();
        // Validate request
        if (webhookRequest == null || webhookRequest.getToken() == null) {
            context.getLogger().fine("Invalid request: token is required");
            throw new IllegalArgumentException("Invalid request: token is required");
        }

        // Validate appID and provider
        if (!config.dittoAppId().equals(webhookRequest.getAppID())) {
            context.getLogger().fine("Invalid appID: " + webhookRequest.getAppID());
            throw new IllegalArgumentException("Invalid appID: " + webhookRequest.getAppID());
        }
        
        if (!config.dittoProvider().equals(webhookRequest.getProvider())) {
            context.getLogger().fine("Invalid provider: " + webhookRequest.getProvider());
            throw new IllegalArgumentException("Invalid provider: " + webhookRequest.getProvider());
        }
    }

    /**
     * Validates the auth provider JWT token and returns the decoded JWT.
     * @param token The JWT token to validate
     * @return The decoded JWT
     * @throws SecurityException if the token is invalid
     */
    private DecodedJWT validateToken(String token) throws SecurityException {
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
                
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException | JwkException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the permissions response for accessible ledgers.
     * @param userId The user ID
     * @param ledgers List of ledgers the user has access to
     * @return DittoWebhookResponse with the appropriate permissions
     */
    private AuthorizeResponse buildAuthResponse(String userId, List<DittoLedger> ledgers) {
        // Create permission rules for read and write
        PermissionRules readRules = new PermissionRules().everything(false);
        PermissionRules writeRules = new PermissionRules().everything(false);
        Map<String, List<String>> readQueries = new HashMap<>();
        Map<String, List<String>> writeQueries = new HashMap<>();
        
        // For each ledger, add read/write permissions for the Accounts collection
        for (DittoLedger ledger : ledgers) {
            String ledgerId = ledger.getId();
            
            // Check if user is a reader or writer
            boolean isReader = ledger.getReaderIds() != null && ledger.getReaderIds().contains(userId);
            boolean isWriter = ledger.getWriterIds() != null && ledger.getWriterIds().contains(userId);
            
            if (isReader || isWriter) {
                readQueries.computeIfAbsent("Ledgers", k -> new ArrayList<>())
                    .add(String.format("_id = '%s'", ledgerId));
                String childQuery = String.format("_id.ledgerId = '%s'", ledgerId);
                readQueries.computeIfAbsent("Accounts", k -> new ArrayList<>())
                    .add(childQuery);
                
                if (isWriter) {
                    writeQueries.computeIfAbsent("Accounts", k -> new ArrayList<>())
                        .add(childQuery);
                }
            }
        }
        
        Permission permissions = new Permission()
            .read(readRules)
            .write(writeRules);
        return new AuthorizeResponse()
            .authenticated(true)
            .userID(userId)
            .expirationSeconds(config.dittoTokenExpireSec())
            .permissions(permissions);
    }
}
