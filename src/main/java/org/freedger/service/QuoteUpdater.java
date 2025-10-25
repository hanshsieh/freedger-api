package org.freedger.service;

import java.io.IOException;

import org.freedger.repository.openexchangerates.OpenExchangeRatesClient;

public class QuoteUpdater {
  private final OpenExchangeRatesClient exchangeRatesClient;

  public QuoteUpdater(OpenExchangeRatesClient openExchangeRatesClient) {
    this.exchangeRatesClient = openExchangeRatesClient;
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
