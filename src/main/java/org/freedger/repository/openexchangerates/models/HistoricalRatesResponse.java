package org.freedger.repository.openexchangerates.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Response model for OpenExchangeRates historical rates API.
 *
 * <p>Contains exchange rate data for a specific date, with all rates relative to the base currency.
 */
@Value
@Builder
@Jacksonized
public class HistoricalRatesResponse {
  /** Optional disclaimer text. */
  @JsonProperty("disclaimer")
  private final String disclaimer;

  /** Optional license information. */
  @JsonProperty("license")
  private final String license;

  /**
   * UNIX timestamp indicating when the rates were published. Note: In JavaScript, multiply by 1000
   * as it uses milliseconds.
   */
  @JsonProperty("timestamp")
  private final Long timestamp;

  /**
   * The base currency code (3-letter ISO currency code) to which all exchange rates are relative
   * (e.g., "USD").
   */
  @JsonProperty("base")
  private final String base;

  /**
   * Map of currency codes to exchange rates. Keys are 3-letter ISO currency codes, values are
   * exchange rates relative to 1 unit of the base currency.
   */
  @JsonProperty("rates")
  private final Map<String, Double> rates;
}
