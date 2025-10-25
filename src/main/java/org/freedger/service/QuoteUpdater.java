package org.freedger.service;

import java.io.IOException;

import org.freedger.config.Config;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.repository.openexchangerates.OpenExchangeRatesClient;

public class QuoteUpdater {
  private static final String SOURCE = "openexchangerates.org";
  private final OpenExchangeRatesClient exchangeRatesClient;
  private final DittoClient dittoClient;
  /**
   * Resource path to the config file.
   */
  private final String configPath;

  public QuoteUpdater(Config config, OpenExchangeRatesClient openExchangeRatesClient, DittoClient dittoClient) {
    this.configPath = config.openExchangeRatesConfigPath();
    this.exchangeRatesClient = openExchangeRatesClient;
    this.dittoClient = dittoClient;
  }
  /**
   * Updates the quotes of all the currencies for the given number of days.
   * 
   * @param maxDays The maximum number of days to update the quotes for.
   */
  public void updateQuotes(int maxDays) throws IOException {
    // TODO
  }
}
