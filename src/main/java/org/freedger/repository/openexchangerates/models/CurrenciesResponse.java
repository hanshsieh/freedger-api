package org.freedger.repository.openexchangerates.models;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
@Value
@Builder
@Jacksonized
public class CurrenciesResponse {
  /**
   * Map of currency codes to their full display names. Keys are typically 3-letter ISO currency
   * codes (or alternative currency identifiers), values are the currency's full name.
   */
  @JsonProperty("currencies")
  private Map<String, String> currencies;
}

