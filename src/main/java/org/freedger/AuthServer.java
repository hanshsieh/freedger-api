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
import org.freedger.dto.*;

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
    private final String tokenAudience;
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
            Env.tokenAudience(),
            Env.tokenSecret1()
        );
    }

    protected AuthServer(
        String authProviderIssuer, 
        String authProviderAudience, 
        JwkProvider authProviderJwks,
        String tokenIssuer, 
        String tokenAudience,
        String exchangeTokenSecret) {
        this.authProviderIssuer = authProviderIssuer;
        this.authProviderAudience = authProviderAudience;
        this.authProviderJwks = authProviderJwks;
        this.tokenIssuer = tokenIssuer;
        this.tokenAudience = tokenAudience;
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
                .withAudience(tokenAudience)
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
            if (webhookRequest == null || webhookRequest.getToken() == null || 
                webhookRequest.getAppId() == null || webhookRequest.getProvider() == null) {
                context.getLogger().warning("Invalid request: token, appID, and provider are required");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            // Validate appID and provider
            String expectedAppId = Env.dittoAppId();
            String expectedProvider = Env.dittoProviderName();
            
            if (!expectedAppId.equals(webhookRequest.getAppId())) {
                context.getLogger().warning("Invalid appID: " + webhookRequest.getAppId());
                return request.createResponseBuilder(HttpStatus.OK)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            if (!expectedProvider.equals(webhookRequest.getProvider())) {
                context.getLogger().warning("Invalid provider: " + webhookRequest.getProvider());
                return request.createResponseBuilder(HttpStatus.OK)
                    .body(DittoWebhookResponse.failure())
                    .build();
            }
            
            // Validate JWT token with specific audience and scope
            DecodedJWT jwt = validateDittoWebhookToken(webhookRequest.getToken());
            String userId = jwt.getSubject();
            
            if (userId == null) {
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid token: missing subject"))
                    .build();
            }
            
            // Get user's accessible ledgers from Ditto
            String dittoBaseUrl = Env.dittoApiBaseUrl();
            if (dittoBaseUrl == null || dittoBaseUrl.isBlank()) {
                throw new IllegalStateException("DITTO_API_BASE_URL environment variable is not set");
            }
            
            DittoHttpClient dittoClient = new DittoHttpClient(
                dittoBaseUrl,
                Env.dittoApiKey()
            );
            
            List<DittoLedger> accessibleLedgers = dittoClient.findAccessibleLedgers(userId);
            
            // Build permissions response
            DittoWebhookResponse response = buildPermissionsResponse(userId, accessibleLedgers);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .build();
                
        } catch (JWTVerificationException | JwkException e) {
            context.getLogger().warning("Token validation failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.OK)
                .body(DittoWebhookResponse.failure())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing Ditto permissions request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.OK)
                .body(DittoWebhookResponse.failure())
                .build();
        }
    }
    
    /**
     * Validates the JWT token for Ditto webhook with specific audience and scope requirements.
     * @param token The JWT token to validate
     * @return The decoded JWT
     * @throws JWTVerificationException if the token is invalid
     * @throws JwkException if there's an error fetching the JWK
     */
    private DecodedJWT validateDittoWebhookToken(String token) throws JWTVerificationException, JwkException {
        // Verify token signature and basic claims
        DecodedJWT jwt = JWT.decode(token);
        
        // Get the key from the JWKS endpoint
        Jwk jwk = authProviderJwks.get(jwt.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        
        // Verify the token with specific audience and required scope
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWT.require(algorithm)
            .withIssuer(authProviderIssuer)
            .withAudience("https://api.freedger.org/") // Required audience for Ditto webhook
            .withClaimPresence("scope") // Ensure scope claim exists
            .acceptLeeway(10);
            
        // Verify the token
        DecodedJWT verifiedJwt = verification.build().verify(token);
        
        // Check for required scope
        String scope = verifiedJwt.getClaim("scope").asString();
        if (scope == null || !scope.contains("read:ditto_perms")) {
            throw new JWTVerificationException("Missing required scope: read:ditto_perms");
        }
        
        return verifiedJwt;
    }
    
    /**
     * Builds the permissions response for accessible ledgers.
     * @param userId The user ID
     * @param ledgers List of ledgers the user has access to
     * @return DittoWebhookResponse with the appropriate permissions
     */
    private DittoWebhookResponse buildPermissionsResponse(String userId, List<DittoLedger> ledgers) {
        // Default to 24 hours expiration
        final int EXPIRATION_SECONDS = 24 * 60 * 60;
        
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
        DittoWebhookResponse.Permission permissions = new DittoWebhookResponse.Permission(readRules, writeRules);
        
        // Create and return the response
        return DittoWebhookResponse.success(userId, EXPIRATION_SECONDS, permissions);
    }
    
    // Error response creation is now handled by JsonUtils
}
