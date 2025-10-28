package org.freedger.controller.utils;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class AppContext {
  private static final ThreadLocal<ExecutionContext> localContext = new ThreadLocal<>();
  private static final Logger fallbackLogger = Logger.getLogger(AppContext.class.getName());

  public static void setContext(@Nullable ExecutionContext context) {
    localContext.set(context);
  }

  @Nullable
  public static ExecutionContext getContext() {
    return localContext.get();
  }

  public static void clearContext() {
    localContext.remove();
  }

  @NotNull
  private static Logger getLogger() {
    final var context = getContext();
    if (context != null) {
      return context.getLogger();
    }
    return fallbackLogger;
  }

  public static void log(Level level, String message, Object... args) {
    log(level, null, message, args);
  }

  public static void log(Level level, Throwable throwable, String message, Object... args) {
    final var logger = getLogger();
    if (!logger.isLoggable(level)) {
      return;
    }
    // Azure functions won't format the message so `Record.setParameters` won't work.
    String formatted = (args == null || args.length == 0)
      ? message
      : MessageFormat.format(message, args);
    final var record = new LogRecord(level, formatted);
    record.setThrown(throwable);
    logger.log(record);
  }
}
