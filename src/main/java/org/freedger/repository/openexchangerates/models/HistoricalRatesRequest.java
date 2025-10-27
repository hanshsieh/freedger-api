package org.freedger.repository.openexchangerates.models;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Request model for fetching historical exchange rates.
 *
 * <p>Use the builder pattern to construct requests:
 *
 * <pre>
 * HistoricalRatesRequest request = HistoricalRatesRequest.builder()
 *     .date(LocalDate.of(2024, 1, 1))
 *     .base("EUR")
 *     .symbols(List.of("USD", "GBP", "JPY"))
 *     .build();
 * </pre>
 */
@Value
@Builder
public class HistoricalRatesRequest {
  /** The date to fetch historical rates for. */
  private final LocalDate date;

  /**
   * Optional base currency (3-letter ISO currency code). Requires Developer, Enterprise or
   * Unlimited plan.
   */
  private final String base;

  /**
   * Optional list of currency codes to limit the response. Requires Developer, Enterprise or
   * Unlimited plan.
   */
  private final List<String> symbols;
}
