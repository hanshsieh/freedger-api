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
import java.util.regex.Pattern;

import org.freedger.ditto.DittoHttpClient;
import org.freedger.ditto.DittoLedger;
import org.freedger.dto.ditto.AuthorizeRequest;
import org.freedger.dto.ditto.AuthorizeResponse;
import org.freedger.dto.ditto.Permission;
import org.freedger.dto.ditto.PermissionRules;

/**
 * Azure Functions with HTTP Trigger for Ditto APIs.
 */
public class DittoApi {
    private final DittoHttpClient dittoClient;
    private final JwkProvider authProviderJwks;
    private final Config config;
    private final ScopePredicate scopePredicate;

    public DittoApi() throws URISyntaxException, MalformedURLException {
        this(EnvConfig.instance);
    }

    protected DittoApi(Config config) throws URISyntaxException, MalformedURLException {
        this(config, 
            createJwkProvider(config.authJwks()), 
            new DittoHttpClient(config.dittoApiBaseUrl(), config.dittoApiKey()));
    }

    protected DittoApi(
        Config config, 
        JwkProvider jwkProvider,
        DittoHttpClient dittoClient) {
        this.config = config;
        this.authProviderJwks = jwkProvider;
        this.dittoClient = dittoClient;
        this.scopePredicate = new ScopePredicate(new String[] { Scope.READ_DITTO_AUTH.getValue() });
    }

    private static JwkProvider createJwkProvider(String url) {
        try {
            return new JwkProviderBuilder(new URI(url).toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .timeouts(5000, 5000)
                .build();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid JWKS URL: " + url, e);
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
                .body(AuthorizeResponse.failure())
                .build();
        } catch (SecurityException e) {
            context.getLogger().warning("Token validation failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(AuthorizeResponse.failure())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing Ditto permissions request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthorizeResponse.failure())
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
        if (!config.dittoAppId().equals(webhookRequest.getAppId())) {
            context.getLogger().fine("Invalid appID: " + webhookRequest.getAppId());
            throw new IllegalArgumentException("Invalid appID: " + webhookRequest.getAppId());
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
        PermissionRules readRules = new PermissionRules(false);
        PermissionRules writeRules = new PermissionRules(false);
        
        // For each ledger, add read/write permissions for the Accounts collection
        for (DittoLedger ledger : ledgers) {
            String ledgerId = ledger.getId();
            
            // Check if user is a reader or writer
            boolean isReader = ledger.getReaderIds() != null && ledger.getReaderIds().contains(userId);
            boolean isWriter = ledger.getWriterIds() != null && ledger.getWriterIds().contains(userId);
            
            if (isReader || isWriter) {
                readRules.addQuery("Ledgers", String.format("_id = '%s'", ledgerId));

                String query = String.format("_id.ledgerId = '%s'", ledgerId);
                readRules.addQuery("Accounts", query);
                
                if (isWriter) {
                    writeRules.addQuery("Accounts", query);
                }
            }
        }
        
        Permission permissions = new Permission(readRules, writeRules);       
        return AuthorizeResponse.success(userId, config.dittoTokenExpireSec(), permissions);
    }
}
