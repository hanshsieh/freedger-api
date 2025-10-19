package org.freedger.services.openexchangerates.models;

import java.time.LocalDate;
import java.util.List;

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

  /** Private constructor. Use builder() to create instances. */
  private HistoricalRatesRequest(Builder builder) {
    this.date = builder.date;
    this.base = builder.base;
    this.symbols = builder.symbols;
  }

  /**
   * Gets the date to fetch historical rates for.
   *
   * @return The date
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Gets the base currency code.
   *
   * @return The base currency code, or null if not set
   */
  public String getBase() {
    return base;
  }

  /**
   * Gets the list of currency codes to limit the response.
   *
   * @return The list of currency codes, or null if not set
   */
  public List<String> getSymbols() {
    return symbols;
  }

  /**
   * Creates a new builder for constructing HistoricalRatesRequest instances.
   *
   * @return A new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for HistoricalRatesRequest. */
  public static class Builder {
    private LocalDate date;
    private String base;
    private List<String> symbols;

    private Builder() {}

    /**
     * Sets the date to fetch historical rates for.
     *
     * @param date The date (required)
     * @return This builder
     */
    public Builder date(LocalDate date) {
      this.date = date;
      return this;
    }

    /**
     * Sets the base currency.
     *
     * <p>Note: Requires Developer, Enterprise or Unlimited plan.
     *
     * @param base The 3-letter ISO currency code (e.g., "EUR")
     * @return This builder
     */
    public Builder base(String base) {
      this.base = base;
      return this;
    }

    /**
     * Sets the currency codes to limit the response.
     *
     * <p>Note: Requires Developer, Enterprise or Unlimited plan.
     *
     * @param symbols List of 3-letter ISO currency codes
     * @return This builder
     */
    public Builder symbols(List<String> symbols) {
      this.symbols = symbols;
      return this;
    }

    /**
     * Sets the currency codes to limit the response.
     *
     * <p>Note: Requires Developer, Enterprise or Unlimited plan.
     *
     * @param symbols Variable arguments of 3-letter ISO currency codes
     * @return This builder
     */
    public Builder symbols(String... symbols) {
      this.symbols = List.of(symbols);
      return this;
    }

    /**
     * Builds the HistoricalRatesRequest instance.
     *
     * @return A new HistoricalRatesRequest instance
     * @throws IllegalArgumentException If date is not set
     */
    public HistoricalRatesRequest build() {
      if (date == null) {
        throw new IllegalArgumentException("date is required");
      }
      return new HistoricalRatesRequest(this);
    }
  }

  @Override
  public String toString() {
    return "HistoricalRatesRequest{"
        + "date="
        + date
        + ", base='"
        + base
        + '\''
        + ", symbols="
        + symbols
        + '}';
  }
}
