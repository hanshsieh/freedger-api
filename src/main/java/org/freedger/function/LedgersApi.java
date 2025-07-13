package org.freedger.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import jakarta.validation.ValidationException;

import javax.inject.Inject;

import org.freedger.openapi.models.ErrorCode;
import org.freedger.openapi.models.ErrorResponse;
import org.freedger.openapi.models.LedgerConfig;

public class LedgersApi {
    private final AppValidator validator;

    @Inject
    public LedgersApi(AppValidator validator) {
        this.validator = validator;
    }
    
    @FunctionName("createLedger")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "ledgers") 
            HttpRequestMessage<LedgerConfig> request,
            final ExecutionContext context) {
        try {
            validator.validate(request.getBody());
            return null;
        } catch (ValidationException e) {
            context.getLogger().fine("Invalid request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse().code(ErrorCode.INVALID_REQUEST).message(e.getMessage()))
                .build();
        } catch (Exception ex) {
            context.getLogger().severe("Error processing create ledger request: " + ex.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse().code(ErrorCode.SERVER_ERROR).message(ex.getMessage()))
                .build();
        }
    }
}
