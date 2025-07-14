package org.freedger.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import jakarta.validation.ValidationException;

import java.time.ZoneOffset;
import java.util.Collections;

import javax.inject.Inject;

import org.freedger.function.utils.HttpMessageSerializer;
import org.freedger.function.utils.RequestValidator;
import org.freedger.function.utils.Scope;
import org.freedger.function.utils.ScopePredicate;
import org.freedger.function.utils.TokenValidator;
import org.freedger.openapi.models.ErrorCode;
import org.freedger.openapi.models.ErrorResponse;
import org.freedger.openapi.models.Ledger;
import org.freedger.openapi.models.LedgerCreate;
import org.freedger.services.ditto.DittoHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedgersApi {
    private static final Logger logger = LoggerFactory.getLogger(LedgersApi.class);
    private final RequestValidator requestValidator;
    private final TokenValidator tokenValidator;
    private final ScopePredicate writeLedgersPredicate;
    private final HttpMessageSerializer httpMessageSerializer;
    private final DittoHttpClient dittoClient;

    @Inject
    public LedgersApi(RequestValidator requestValidator, 
        TokenValidator tokenValidator, 
        HttpMessageSerializer httpMessageSerializer, 
        DittoHttpClient dittoClient) {
        this.requestValidator = requestValidator;
        this.tokenValidator = tokenValidator;
        this.writeLedgersPredicate = new ScopePredicate(new String[] { Scope.WRITE_LEDGERS.getValue() });
        this.httpMessageSerializer = httpMessageSerializer;
        this.dittoClient = dittoClient;
    }
    
    @FunctionName("CreateLedger")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "ledgers") 
            HttpRequestMessage<LedgerCreate> request,
            final ExecutionContext context) {
        try {
            context.getLogger().severe("Creating ledger (with Azure Functions)");
            logger.error("Creating ledger (with SLF4J)");
            requestValidator.validate(request.getBody());
            final var jwtToken = tokenValidator.validate(request, writeLedgersPredicate);
            final var reqLedgerCreate = request.getBody();
            final var dittoLedgerCreate = new org.freedger.services.ditto.models.LedgerCreate();
            dittoLedgerCreate.setName(reqLedgerCreate.getName());
            dittoLedgerCreate.setNote(reqLedgerCreate.getNote());
            dittoLedgerCreate.setCurrencyId(reqLedgerCreate.getCurrencyId());
            dittoLedgerCreate.setExternalAccountName(reqLedgerCreate.getExternalAccountName());
            dittoLedgerCreate.setReaderIds(Collections.emptyList());
            dittoLedgerCreate.setWriterIds(Collections.singletonList(jwtToken.getSubject()));
            
            final var dittoLedger = dittoClient.createLedger(dittoLedgerCreate);
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
                    request.createResponseBuilder(HttpStatus.CREATED), respLedger)
                .build();
        } catch (ValidationException e) {
            logger.debug("Invalid request: {}", e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
                .build();
        } catch (SecurityException e) {
            logger.info("Token validation failed: {}", e.getMessage());
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse().code(ErrorCode.UNAUTHORIZED).message(e.getMessage()))
                .build();
        } catch (Exception ex) {
            logger.error("Error processing create ledger request: {}", ex.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
                .build();
        }
    }
}
