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

import org.freedger.ditto.DittoHttpClient;
import org.freedger.ditto.DittoLedger;
import org.freedger.dto.ditto.DittoWebhookRequest;
import org.freedger.dto.ditto.DittoWebhookResponse;
import org.freedger.dto.ditto.Permission;
import org.freedger.dto.ditto.PermissionRules;

/**
 * Azure Functions with HTTP Trigger for Ditto Token Exchange.
 */
public class AuthServer {
    private final DittoHttpClient dittoClient;
    private final JwkProvider authProviderJwks;
    private final Config config;

    public AuthServer() {
        this(EnvConfig.instance);
    }

    protected AuthServer(Config config) {
        this(config, new JwkProviderBuilder(config.authJwks())
            .cached(10, 24, TimeUnit.HOURS)
            .build());
    }

    protected AuthServer(
        Config config, 
        JwkProvider authProviderJwks) {
        this.config = config;
        this.authProviderJwks = authProviderJwks;
        this.dittoClient = new DittoHttpClient(config.dittoApiBaseUrl(), config.dittoApiKey());
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
                .withIssuer(config.authIssuer())
                .withAudience(config.authAudience())
                .acceptLeeway(10)
                .build();
                
            return verifier.verify(token);
        } catch (JWTVerificationException | JwkException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the Ditto webhook authentication request.
     * Validates the request and returns the user's permissions.
     * 
     * @param request The incoming HTTP request
     * @param context The execution context
     * @return HTTP response with the user's permissions or an error message
     */
    @FunctionName("GetDittoPermissions")
    public HttpResponseMessage getDittoPermissions(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<DittoWebhookRequest> request,
            final ExecutionContext context) {
        
        try {
            // Get request body
            DittoWebhookRequest webhookRequest = request.getBody();
            
            // Validate request
            if (webhookRequest == null || webhookRequest.getToken() == null) {
                context.getLogger().fine("Invalid request: token is required");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            // Validate appID and provider
            if (!config.dittoAppId().equals(webhookRequest.getAppId())) {
                context.getLogger().fine("Invalid appID: " + webhookRequest.getAppId());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            if (!config.dittoProvider().equals(webhookRequest.getProvider())) {
                context.getLogger().fine("Invalid provider: " + webhookRequest.getProvider());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            // Validate JWT token with specific audience and scope
            DecodedJWT jwt = validateAuthProviderToken(webhookRequest.getToken());
            String userId = jwt.getSubject();
            
            if (userId == null) {
                throw new SecurityException("Invalid token: missing subject");
            }
            
            List<DittoLedger> accessibleLedgers = dittoClient.findAccessibleLedgers(userId);

            // Build permissions response
            DittoWebhookResponse response = buildPermissionsResponse(userId, accessibleLedgers);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .build();
                
        } catch (SecurityException e) {
            context.getLogger().warning("Token validation failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(DittoWebhookResponse.failure())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing Ditto permissions request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DittoWebhookResponse.failure())
                .build();
        }
    }
    
    /**
     * Builds the permissions response for accessible ledgers.
     * @param userId The user ID
     * @param ledgers List of ledgers the user has access to
     * @return DittoWebhookResponse with the appropriate permissions
     */
    private DittoWebhookResponse buildPermissionsResponse(String userId, List<DittoLedger> ledgers) {
        // Create permission rules for read and write
        PermissionRules readRules = new PermissionRules(false);
        PermissionRules writeRules = new PermissionRules(false);
        
        // For each ledger, add read/write permissions for the Accounts collection
        for (DittoLedger ledger : ledgers) {
            String ledgerId = ledger.getId();
            
            // Check if user is a reader or writer
            boolean isReader = ledger.getReaderIds() != null && ledger.getReaderIds().contains(userId);
            boolean isWriter = ledger.getWriterIds() != null && ledger.getWriterIds().contains(userId);
            
            if (isReader || isWriter) {
                String query = String.format("_id.ledgerId = '%s'", ledgerId);
                readRules.addQuery("Accounts", query);
                
                if (isWriter) {
                    writeRules.addQuery("Accounts", query);
                }
            }
        }
        
        // Create permissions object
        Permission permissions = new Permission(readRules, writeRules);
        
        // Create and return the response
        return DittoWebhookResponse.success(userId, config.dittoTokenExpireSec(), permissions);
    }
}
