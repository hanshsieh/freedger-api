package org.freedger.controller;

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
import org.freedger.domain.models.CollectionQuery;
import org.freedger.domain.models.DittoAuthRequest;
import org.freedger.domain.models.LedgerChildOrGlobalQuery;
import org.freedger.domain.models.LedgerChildQuery;
import org.freedger.domain.models.Scope;
import org.freedger.domain.models.ScopePredicate;
import org.freedger.openapi.models.AuthorizeRequest;
import org.freedger.openapi.models.AuthorizeResponse;
import org.freedger.openapi.models.DittoAuthToken;
import org.freedger.service.AuthService;
import org.freedger.service.HttpMessageSerializer;
import org.freedger.service.RequestValidator;
import org.freedger.service.TokenValidator;

/** Azure Functions with HTTP Trigger for Ditto APIs. */
public class DittoApi {
  private final RequestValidator requestValidator;
  private final AuthService authService;
  private final Config config;
  private final TokenValidator tokenValidator;
  private final ScopePredicate scopePredicate;
  private final HttpMessageSerializer serializer;

  @Inject
  public DittoApi(
      RequestValidator validator,
      Config config,
      AuthService authService,
      TokenValidator tokenValidator,
      HttpMessageSerializer serializer) {
    this.requestValidator = validator;
    this.config = config;
    this.authService = authService;
    this.tokenValidator = tokenValidator;
    this.scopePredicate = new ScopePredicate(new String[] {Scope.READ_DITTO_AUTH.getValue()});
    this.serializer = serializer;
  }

  /**
   * Handles the Ditto webhook authentication request. Validates the request and returns the user's
   * permissions.
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
      DittoAuthToken dittoAuthToken =
          serializer.deserialize(requestBody.getToken(), DittoAuthToken.class);

      // Validate JWT token with specific audience and scope
      DecodedJWT jwt = tokenValidator.validate(dittoAuthToken.getAccessToken(), scopePredicate);
      String userId = jwt.getSubject();

      if (userId == null) {
        throw new SecurityException("Invalid token: missing subject");
      }

      final var response = authService.dittoAuthorize(DittoAuthRequest.builder()
        .userId(userId)
        .transactionId(dittoAuthToken.getTransactionId())
        .build());
      return request.createResponseBuilder(HttpStatus.OK).body(response).build();
    } catch (ValidationException | IllegalArgumentException e) {
      logger.fine("Invalid request: " + e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body(new AuthorizeResponse().authenticate(false))
          .build();
    } catch (SecurityException e) {
      logger.info("Token validation failed: " + e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.UNAUTHORIZED)
          .body(new AuthorizeResponse().authenticate(false))
          .build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error processing Ditto permissions request", e);
      return request
          .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AuthorizeResponse().authenticate(false))
          .build();
    }
  }

  private void validateRequest(
      ExecutionContext context, HttpRequestMessage<AuthorizeRequest> request) {
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
}
