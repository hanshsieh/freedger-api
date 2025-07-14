package org.freedger.function;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
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

import jakarta.validation.ValidationException;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.freedger.config.Config;
import org.freedger.openapi.models.AuthorizeRequest;
import org.freedger.openapi.models.AuthorizeResponse;
import org.freedger.openapi.models.Permission;
import org.freedger.openapi.models.PermissionRules;
import org.freedger.services.ditto.DittoHttpClient;
import org.freedger.services.ditto.models.Ledger;

/**
 * Azure Functions with HTTP Trigger for Ditto APIs.
 */
public class DittoApi {

    private static class CollectionQuery {
        public final String name;

        public CollectionQuery(String name) {
            this.name = name;
        }

        public Optional<String> forReader(String ledgerId) {
            return Optional.of(String.format("_id.ledgerId = '%s'", ledgerId));
        }
        public Optional<String> forWriter(String ledgerId) {
            return Optional.of(String.format("_id.ledgerId = '%s'", ledgerId));
        }
    }

    private static final List<CollectionQuery> collections = List.of(
        new CollectionQuery("AccountGroups"),
        new CollectionQuery("Accounts"),
        new CollectionQuery("Categories"),
        new CollectionQuery("CategoryGroups"),
        // For custom currencies
        new CollectionQuery("Currencies"),
        // For system defined currencies
        new CollectionQuery("Currencies") {
            @Override
            public Optional<String> forReader(String ledgerId) {
                return Optional.of("_id.ledgerId IS MISSING");
            }
            @Override
            public Optional<String> forWriter(String ledgerId) {
                return Optional.empty();
            }
        },
        new CollectionQuery("JournalEntries"),
        new CollectionQuery("Ledgers") {
            @Override
            public Optional<String> forReader(String ledgerId) {
                return Optional.of(String.format("_id = '%s'", ledgerId));
            }
            @Override
            public Optional<String> forWriter(String ledgerId) {
                return Optional.empty();
            }
        },
        new CollectionQuery("Platforms"),
        new CollectionQuery("Projects"),
        new CollectionQuery("Symbols"),
        new CollectionQuery("Tags"),
        new CollectionQuery("Transactions"),
        new CollectionQuery("Users")
    );

    private final RequestValidator requestValidator;
    private final DittoHttpClient dittoClient;
    private final Config config;
    private final TokenValidator tokenValidator;
    private final ScopePredicate scopePredicate;

    @Inject
    public DittoApi(
        RequestValidator validator,
        Config config, 
        DittoHttpClient dittoClient,
        TokenValidator tokenValidator) {
        this.requestValidator = validator;
        this.config = config;
        this.dittoClient = dittoClient;
        this.tokenValidator = tokenValidator;
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
            DecodedJWT jwt = tokenValidator.validate(webhookRequest.getToken(), scopePredicate);
            String userId = jwt.getSubject();
            
            if (userId == null) {
                throw new SecurityException("Invalid token: missing subject");
            }
            
            List<Ledger> accessibleLedgers = dittoClient.findAccessibleLedgers(userId);

            AuthorizeResponse response = buildAuthResponse(userId, accessibleLedgers);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .build();
        } catch (ValidationException e) {
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
            context.getLogger().severe("Error processing Ditto permissions request: " + ExceptionUtil.getPrettyStackTrace(e));
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthorizeResponse().authenticated(false))
                .build();
        }
    }

    private void validateRequest(ExecutionContext context, HttpRequestMessage<AuthorizeRequest> request) {
        AuthorizeRequest webhookRequest = request.getBody();

        requestValidator.validate(webhookRequest);

        // Validate request
        if (webhookRequest == null || webhookRequest.getToken() == null) {
            context.getLogger().fine("Invalid request: token is required");
            throw new ValidationException("Invalid request: token is required");
        }

        // Validate appID and provider
        if (!config.dittoAppId().equals(webhookRequest.getAppID())) {
            context.getLogger().fine("Invalid appID: " + webhookRequest.getAppID());
            throw new ValidationException("Invalid appID: " + webhookRequest.getAppID());
        }
        
        if (!config.dittoProvider().equals(webhookRequest.getProvider())) {
            context.getLogger().fine("Invalid provider: " + webhookRequest.getProvider());
            throw new ValidationException("Invalid provider: " + webhookRequest.getProvider());
        }
    }

    /**
     * Builds the permissions response for accessible ledgers.
     * @param userId The user ID
     * @param ledgers List of ledgers the user has access to
     * @return DittoWebhookResponse with the appropriate permissions
     */
    private AuthorizeResponse buildAuthResponse(String userId, List<Ledger> ledgers) {
        Map<String, Set<String>> readQueries = new HashMap<>();
        Map<String, Set<String>> writeQueries = new HashMap<>();

        for (var collection : collections) {
            final var collectionReads = readQueries.computeIfAbsent(collection.name, k -> new HashSet<>());
            final var collectionWrites = writeQueries.computeIfAbsent(collection.name, k -> new HashSet<>());

            for (Ledger ledger : ledgers) {
                String ledgerId = ledger.getId();
                
                // Check if user is a reader or writer
                boolean isWriter = ledger.getWriterIds() != null && ledger.getWriterIds().contains(userId);
                boolean isReader = isWriter || ledger.getReaderIds() != null && ledger.getReaderIds().contains(userId);

                if (isReader) {
                    collection.forReader(ledgerId).ifPresent(collectionReads::add);
                }

                if (isWriter) {
                    collection.forWriter(ledgerId).ifPresent(collectionWrites::add);
                }
            }
        }

        // Create permission rules for read and write
        var readRules = new PermissionRules()
            .everything(false)
            .queriesByCollection(readQueries.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    e -> e.getValue().stream().sorted().collect(Collectors.toList()))));
        var writeRules = new PermissionRules()
            .everything(false)
            .queriesByCollection(writeQueries.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    e -> e.getValue().stream().sorted().collect(Collectors.toList()))));

        var permissions = new Permission()
            .read(readRules)
            .write(writeRules);
        return new AuthorizeResponse()
            .authenticated(true)
            .userID(userId)
            .expirationSeconds(config.dittoTokenExpireSec())
            .permissions(permissions);
    }
}
