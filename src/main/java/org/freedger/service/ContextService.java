package org.freedger.service;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class ContextService {
  private final ThreadLocal<ExecutionContext> executionContext = new ThreadLocal<>();
  private static final Logger fallbackLogger = Logger.getLogger(ContextService.class.getName());

  public void setContext(@Nullable ExecutionContext executionContext) {
    this.executionContext.set(executionContext);
  }

  @Nullable
  public ExecutionContext getContext() {
    return executionContext.get();
  }

  public void clearContext() {
    executionContext.remove();
  }

  @NotNull
  public Logger getLogger() {
    final var context = getContext();
    if (context != null) {
      return context.getLogger();
    }
    return fallbackLogger;
  }

  public void log(Level level, String message, Object... args) {
    loge(level, null, message, args);
  }

  public void loge(Level level, Throwable throwable, String message, Object... args) {
    final var logger = getLogger();
    if (logger.isLoggable(level)) {
      return;
    }
    final var record = new LogRecord(level, message);
    record.setParameters(args);
    record.setThrown(throwable);
    logger.log(record);
  }
}
