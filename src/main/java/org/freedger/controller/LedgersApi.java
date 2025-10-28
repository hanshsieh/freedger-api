package org.freedger.controller;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.validation.ValidationException;
import java.util.logging.Level;
import javax.inject.Inject;

import org.freedger.controller.utils.AppContext;
import org.freedger.domain.exception.NotFoundException;
import org.freedger.domain.models.CreateLedgerRequest;
import org.freedger.domain.models.Scope;
import org.freedger.domain.models.ScopePredicate;
import org.freedger.domain.models.UpdateLedgerRequest;
import org.freedger.openapi.models.CreateLedger;
import org.freedger.openapi.models.ErrorCode;
import org.freedger.openapi.models.ErrorResponse;
import org.freedger.openapi.models.UpdateLedger;
import org.freedger.service.HttpMessageSerializer;
import org.freedger.service.LedgerService;
import org.freedger.service.RequestValidator;
import org.freedger.service.TokenValidator;

public class LedgersApi {
  private final RequestValidator requestValidator;
  private final TokenValidator tokenValidator;
  private final ScopePredicate writeLedgersPredicate;
  private final HttpMessageSerializer httpMessageSerializer;
  private final LedgerService ledgerService;

  @Inject
  public LedgersApi(
      RequestValidator requestValidator,
      TokenValidator tokenValidator,
      HttpMessageSerializer httpMessageSerializer,
      LedgerService ledgerService) {
    this.requestValidator = requestValidator;
    this.tokenValidator = tokenValidator;
    this.writeLedgersPredicate = new ScopePredicate(new String[] {Scope.WRITE_LEDGERS.getValue()});
    this.httpMessageSerializer = httpMessageSerializer;
    this.ledgerService = ledgerService;
  }

  @FunctionName("CreateLedger")
  public HttpResponseMessage createLedger(
      @HttpTrigger(
              name = "req",
              methods = {HttpMethod.POST},
              authLevel = AuthorizationLevel.ANONYMOUS,
              route = "ledgers")
          HttpRequestMessage<CreateLedger> request,
      final ExecutionContext context) {
    try {
      AppContext.setContext(context);
      requestValidator.validate(request.getBody());
      final var jwtToken = tokenValidator.validate(request, writeLedgersPredicate);
      final var reqLedgerCreate = request.getBody();
      final var createdLedger = ledgerService.createLedger(CreateLedgerRequest.builder()
        .name(reqLedgerCreate.getName())
        .note(reqLedgerCreate.getNote())
        .currencyId(reqLedgerCreate.getCurrencyId())
        .externalAccountName(reqLedgerCreate.getExternalAccountName())
        .userId(jwtToken.getSubject())
        .build());
      return httpMessageSerializer
          .serializeResponse(
              request.createResponseBuilder(HttpStatus.CREATED),
              createdLedger.getData().toOpenApiModel(),
              createdLedger.getTransactionId())
          .build();
    } catch (ValidationException e) {
      AppContext.log(Level.FINE, "Invalid request: {0}", e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
          .build();
    } catch (SecurityException e) {
      AppContext.log(Level.INFO, "Token validation failed: {0}", e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse().code(ErrorCode.UNAUTHORIZED).message(e.getMessage()))
          .build();
    } catch (Exception ex) {
      AppContext.log(Level.SEVERE, ex, "Error processing create ledger request");
      return request
          .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
          .build();
    } finally {
      AppContext.clearContext();
    }
  }

  @FunctionName("UpdateLedger")
  public HttpResponseMessage updateLedger(
      @HttpTrigger(
              name = "req",
              methods = {HttpMethod.PUT},
              authLevel = AuthorizationLevel.ANONYMOUS,
              route = "ledgers/{ledgerId}")
          HttpRequestMessage<UpdateLedger> request,
      @BindingName("ledgerId") String ledgerId,
      final ExecutionContext context) {
    final var logger = context.getLogger();
    try {
      AppContext.setContext(context);
      if (ledgerId == null) {
        throw new ValidationException("Ledger ID is required");
      }
      requestValidator.validate(request.getBody());
      final var jwtToken = tokenValidator.validate(request, writeLedgersPredicate);
      final var reqLedgerUpdate = request.getBody();
      final var updatedLedger = ledgerService.updateLedger(UpdateLedgerRequest.builder()
        .id(ledgerId)
        .userId(jwtToken.getSubject())
        .name(reqLedgerUpdate.getName())
        .note(reqLedgerUpdate.getNote())
        .currencyId(reqLedgerUpdate.getCurrencyId())
        .externalAccountId(reqLedgerUpdate.getExternalAccountId())
        .readerIds(reqLedgerUpdate.getReaderIds())
        .writerIds(reqLedgerUpdate.getWriterIds())
        .build());
      return httpMessageSerializer
        .serializeResponse(
          request.createResponseBuilder(HttpStatus.OK),
          updatedLedger.getData().toOpenApiModel(),
          updatedLedger.getTransactionId())
        .build();
    } catch (ValidationException e) {
      logger.fine("Invalid request: " + e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
          .build();
    } catch (SecurityException e) {
      logger.info("Token validation failed: " + e.getMessage());
      return request
          .createResponseBuilder(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse().code(ErrorCode.UNAUTHORIZED).message(e.getMessage()))
          .build();
    } catch (NotFoundException e) {
      return request
          .createResponseBuilder(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponse()
                  .code(ErrorCode.NOT_FOUND)
                  .message("The ledger doesn't exist or you aren't authorized to update it"))
          .build();
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Error processing create ledger request", ex);
      return request
          .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
          .build();
    } finally {
      AppContext.clearContext();
    }
  }
}
