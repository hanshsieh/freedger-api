package org.freedger.controller;

import java.util.logging.Level;

import javax.inject.Inject;

import org.freedger.config.Config;
import org.freedger.controller.utils.AppContext;
import org.freedger.service.QuoteUpdater;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class BackgroundWorker {
  private final Config config;
  private final QuoteUpdater quoteUpdater;

  @Inject
  BackgroundWorker(Config config, QuoteUpdater quoteUpdater) {
    this.config = config;
    this.quoteUpdater = quoteUpdater;
  }

  @FunctionName("UpdateQuotes")
  public void updateQuotes(
      @TimerTrigger(name = "timer", schedule = "0 0 3 * * *") String timerInfo,
      // Uncomment the HttpTrigger and comment out the TimerTrigger to test locally
      /*@HttpTrigger(
        name = "req", 
        methods = {HttpMethod.GET}, 
        route = "background/update-quotes",
        authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,*/
      final ExecutionContext context) throws Exception {
    try {
      AppContext.setContext(context);
      quoteUpdater.updateQuotes(config.quotesUpdateDays());
    } catch (Exception ex) {
      AppContext.log(Level.SEVERE, ex, "Error updating quotes");
      throw ex;
    } finally {
      AppContext.clearContext();
    }
  }

}
