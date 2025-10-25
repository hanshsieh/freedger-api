package org.freedger.repository.openexchangerates.models;

import java.util.Collections;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for OpenExchangeRates currencies API.
 *
 * <p>Contains a map of currency symbols to their full display names. Each key:value pair represents
 * a currency's 3-letter code and its unit display name (singular and capitalized).
 *
 * <p>Example: {"USD": "United States Dollar", "EUR": "Euro", "JPY": "Japanese Yen"}
 *
 * <p>Note: Alternative currencies may not always use a three-letter code.
 */
public class CurrenciesResponse {
  /**
   * Map of currency codes to their full display names. Keys are typically 3-letter ISO currency
   * codes (or alternative currency identifiers), values are the currency's full name.
   */
  @SerializedName("currencies")
  private Map<String, String> currencies;

  /**
   * Default constructor for JSON deserialization.
   */
  public CurrenciesResponse() {}

  /**
   * Creates a new CurrenciesResponse with the given currencies map.
   *
   * @param currencies Map of currency codes to display names
   */
  public CurrenciesResponse(Map<String, String> currencies) {
    this.currencies = currencies;
  }

  /**
   * Gets the currencies map.
   *
   * @return Map of currency codes to their full display names
   */
  public Map<String, String> getCurrencies() {
    return currencies != null ? currencies : Collections.emptyMap();
  }

  /**
   * Sets the currencies map.
   *
   * @param currencies Map of currency codes to display names
   */
  public void setCurrencies(Map<String, String> currencies) {
    this.currencies = currencies;
  }
}

