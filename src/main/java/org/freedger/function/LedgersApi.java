package org.freedger.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import jakarta.validation.ValidationException;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.logging.Level;

import javax.inject.Inject;

import org.freedger.function.utils.HttpMessageSerializer;
import org.freedger.function.utils.RequestValidator;
import org.freedger.function.utils.Scope;
import org.freedger.function.utils.ScopePredicate;
import org.freedger.function.utils.TokenValidator;
import org.freedger.openapi.models.CreateLedger;
import org.freedger.openapi.models.ErrorCode;
import org.freedger.openapi.models.ErrorResponse;
import org.freedger.openapi.models.Ledger;
import org.freedger.openapi.models.UpdateLedger;
import org.freedger.services.ditto.DittoClient;
import org.freedger.services.ditto.exceptions.DittoNotFoundException;

public class LedgersApi {
  private final RequestValidator requestValidator;
  private final TokenValidator tokenValidator;
  private final ScopePredicate writeLedgersPredicate;
  private final HttpMessageSerializer httpMessageSerializer;
  private final DittoClient dittoClient;

  @Inject
  public LedgersApi(RequestValidator requestValidator, 
      TokenValidator tokenValidator, 
      HttpMessageSerializer httpMessageSerializer, 
      DittoClient dittoClient) {
    this.requestValidator = requestValidator;
    this.tokenValidator = tokenValidator;
    this.writeLedgersPredicate = new ScopePredicate(new String[] { Scope.WRITE_LEDGERS.getValue() });
    this.httpMessageSerializer = httpMessageSerializer;
    this.dittoClient = dittoClient;
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
    final var logger = context.getLogger();
    try {
      requestValidator.validate(request.getBody());
      final var jwtToken = tokenValidator.validate(request, writeLedgersPredicate);
      final var reqLedgerCreate = request.getBody();
      final var dittoLedgerCreate = new org.freedger.services.ditto.models.CreateLedger();
      dittoLedgerCreate.setName(reqLedgerCreate.getName());
      dittoLedgerCreate.setNote(reqLedgerCreate.getNote());
      dittoLedgerCreate.setCurrencyId(reqLedgerCreate.getCurrencyId());
      dittoLedgerCreate.setExternalAccountName(reqLedgerCreate.getExternalAccountName());
      dittoLedgerCreate.setReaderIds(Collections.emptyList());
      dittoLedgerCreate.setWriterIds(Collections.singletonList(jwtToken.getSubject()));
      
      final var dittoResp = dittoClient.createLedger(dittoLedgerCreate);
      final var dittoLedger = dittoResp.getData();
      final var respLedger = new Ledger();
      respLedger.setId(dittoLedger.getId());
      respLedger.setCreatedAt(dittoLedger.getCreatedAt().atOffset(ZoneOffset.UTC));
      respLedger.setUpdatedAt(dittoLedger.getUpdatedAt().atOffset(ZoneOffset.UTC));
      respLedger.setName(dittoLedger.getName());
      respLedger.setReaderIds(dittoLedger.getReaderIds());
      respLedger.setWriterIds(dittoLedger.getWriterIds());
      respLedger.setNote(dittoLedger.getNote());
      respLedger.setCurrencyId(dittoLedger.getCurrencyId());
      respLedger.setExternalAccountId(dittoLedger.getExternalAccountId());
      return httpMessageSerializer.serializeResponse(
          request.createResponseBuilder(HttpStatus.CREATED), respLedger, dittoResp.getTransactionId())
          .build();
    } catch (ValidationException e) {
      logger.fine("Invalid request: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
          .build();
    } catch (SecurityException e) {
      logger.info("Token validation failed: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse().code(ErrorCode.UNAUTHORIZED).message(e.getMessage()))
          .build();
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Error processing create ledger request", ex);
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
          .build();
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
      if (ledgerId == null) {
        throw new ValidationException("Ledger ID is required");
      }
      requestValidator.validate(request.getBody());
      final var jwtToken = tokenValidator.validate(request, writeLedgersPredicate);
      final var reqLedgerUpdate = request.getBody();
      final var dittoLedger = new org.freedger.services.ditto.models.UpdateLedger() {{
        setId(ledgerId);
        setUserId(jwtToken.getSubject());
        setName(reqLedgerUpdate.getName());
        setNote(reqLedgerUpdate.getNote());
        setCurrencyId(reqLedgerUpdate.getCurrencyId());
        setExternalAccountId(reqLedgerUpdate.getExternalAccountId());
        setReaderIds(reqLedgerUpdate.getReaderIds());
        setWriterIds(reqLedgerUpdate.getWriterIds());
      }};

      final var updateResp = dittoClient.updateLedger(dittoLedger);
      final var resultResp = dittoClient.getLedger(new org.freedger.services.ditto.models.GetLedger() {{
        setId(ledgerId);
        setUserId(jwtToken.getSubject());
        setTransactionId(updateResp.getTransactionId());
      }});
      final var resultLedger = resultResp.getData();
      final var respLedger = new Ledger();
      respLedger.setId(resultLedger.getId());
      respLedger.setCreatedAt(resultLedger.getCreatedAt().atOffset(ZoneOffset.UTC));
      respLedger.setUpdatedAt(resultLedger.getUpdatedAt().atOffset(ZoneOffset.UTC));
      respLedger.setName(resultLedger.getName());
      respLedger.setReaderIds(resultLedger.getReaderIds());
      respLedger.setWriterIds(resultLedger.getWriterIds());
      respLedger.setNote(resultLedger.getNote());
      respLedger.setCurrencyId(resultLedger.getCurrencyId());
      respLedger.setExternalAccountId(resultLedger.getExternalAccountId());
      return httpMessageSerializer.serializeResponse(
          request.createResponseBuilder(HttpStatus.OK), respLedger, resultResp.getTransactionId())
          .build();
    } catch (ValidationException e) {
      logger.fine("Invalid request: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
          .build();
    } catch (SecurityException e) {
      logger.info("Token validation failed: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse().code(ErrorCode.UNAUTHORIZED).message(e.getMessage()))
          .build();
    } catch (DittoNotFoundException e) {
      return request.createResponseBuilder(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse()
            .code(ErrorCode.NOT_FOUND)
            .message("The ledger doesn't exist or you aren't authorized to update it"))
          .build();
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Error processing create ledger request", ex);
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
          .build();
    }
  }
}
