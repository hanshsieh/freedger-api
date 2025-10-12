package org.freedger.function;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import jakarta.validation.ValidationException;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.freedger.config.Config;
import org.freedger.function.utils.CollectionQuery;
import org.freedger.function.utils.LedgerChildOrGlobalQuery;
import org.freedger.function.utils.HttpMessageSerializer;
import org.freedger.function.utils.LedgerChildQuery;
import org.freedger.function.utils.RequestValidator;
import org.freedger.function.utils.Scope;
import org.freedger.function.utils.ScopePredicate;
import org.freedger.function.utils.TokenValidator;
import org.freedger.openapi.models.AuthorizeRequest;
import org.freedger.openapi.models.AuthorizeResponse;
import org.freedger.openapi.models.DittoAuthToken;
import org.freedger.openapi.models.Permission;
import org.freedger.openapi.models.PermissionRules;
import org.freedger.services.ditto.DittoClient;
import org.freedger.services.ditto.models.Ledger;

/**
 * Azure Functions with HTTP Trigger for Ditto APIs.
 */
public class DittoApi {
    private static final List<CollectionQuery> collections = List.of(
        new LedgerChildQuery("AccountChannels"),
        new LedgerChildQuery("AccountGroups"),
        new LedgerChildQuery("Accounts"),
        new LedgerChildQuery("Categories"),
        new LedgerChildQuery("CategoryGroups"),
        new LedgerChildOrGlobalQuery("Currencies"),
        new LedgerChildOrGlobalQuery("Instruments"),
        new LedgerChildQuery("Journals"),
        new CollectionQuery("Ledgers") {
            @Override
            public List<String> forReader(List<String> ledgerIds) {
                return ledgerIds.stream()
                    .map(id -> String.format("_id == '%s'", id))
                    .collect(Collectors.toList());
            }
            @Override
            public List<String> forWriter(List<String> ledgerIds) {
                return Collections.emptyList();
            }
        },
        new LedgerChildQuery("Platforms"),
        new LedgerChildQuery("ProjectGroups"),
        new LedgerChildQuery("Projects"),
        new LedgerChildOrGlobalQuery("Quotes"),
        new LedgerChildQuery("Tags"),
        new LedgerChildQuery("Transactions")
    );

    private final RequestValidator requestValidator;
    private final DittoClient dittoClient;
    private final Config config;
    private final TokenValidator tokenValidator;
    private final ScopePredicate scopePredicate;
    private final HttpMessageSerializer serializer;

    @Inject
    public DittoApi(
        RequestValidator validator,
        Config config, 
        DittoClient dittoClient,
        TokenValidator tokenValidator,
        HttpMessageSerializer serializer) {
        this.requestValidator = validator;
        this.config = config;
        this.dittoClient = dittoClient;
        this.tokenValidator = tokenValidator;
        this.scopePredicate = new ScopePredicate(new String[] { Scope.READ_DITTO_AUTH.getValue() });
        this.serializer = serializer;
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
        final var logger = context.getLogger();
        try {            
            // Validate request
            validateRequest(context, request);

            // Get request body
            AuthorizeRequest requestBody = request.getBody();
            DittoAuthToken dittoAuthToken = serializer.deserialize(requestBody.getToken(), DittoAuthToken.class);
            
            // Validate JWT token with specific audience and scope
            DecodedJWT jwt = tokenValidator.validate(dittoAuthToken.getAccessToken(), scopePredicate);
            String userId = jwt.getSubject();
            
            if (userId == null) {
                throw new SecurityException("Invalid token: missing subject");
            }
            
            List<Ledger> accessibleLedgers = dittoClient.findAccessibleLedgers(userId, dittoAuthToken.getTransactionId());

            AuthorizeResponse response = buildAuthResponse(userId, accessibleLedgers);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .build();
        } catch (ValidationException | IllegalArgumentException e) {
            logger.fine("Invalid request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(new AuthorizeResponse().authenticate(false))
                .build();
        } catch (SecurityException e) {
            logger.info("Token validation failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(new AuthorizeResponse().authenticate(false))
                .build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing Ditto permissions request", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthorizeResponse().authenticate(false))
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
        final var readQueries = new HashMap<String, List<String>>();
        final var writeQueries = new HashMap<String, List<String>>();
        final var ledgerIds = ledgers.stream().map(Ledger::getId).collect(Collectors.toList());

        for (var collection : collections) {
            readQueries.put(collection.name, collection.forReader(ledgerIds));
            writeQueries.put(collection.name, collection.forWriter(ledgerIds));
        }

        // Create permission rules for read and write
        final var readRules = new PermissionRules()
            .everything(false)
            .queriesByCollection(readQueries.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    e -> e.getValue().stream().sorted().collect(Collectors.toList()))));
        final var writeRules = new PermissionRules()
            .everything(false)
            .queriesByCollection(writeQueries.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    e -> e.getValue().stream().sorted().collect(Collectors.toList()))));

        final var permissions = new Permission()
            .read(readRules)
            .write(writeRules);
        return new AuthorizeResponse()
            .authenticate(true)
            .userID(userId)
            .expirationSeconds(config.dittoTokenExpireSec())
            .permissions(permissions);
    }
}
