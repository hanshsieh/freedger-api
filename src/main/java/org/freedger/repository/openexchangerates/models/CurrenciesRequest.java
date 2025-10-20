package org.freedger.repository.openexchangerates.models;

/**
 * Request model for fetching currency list.
 *
 * <p>Use the builder pattern to construct requests:
 *
 * <pre>
 * CurrenciesRequest request = CurrenciesRequest.builder()
 *     .setShowAlternative(true)
 *     .setShowInactive(true)
 *     .build();
 * </pre>
 */
public class CurrenciesRequest {
  /**
   * Include alternative currencies. Default is false.
   */
  private final boolean showAlternative;

  /**
   * Include historical/inactive currencies. Default is false.
   */
  private final boolean showInactive;

  /** Private constructor. Use builder() to create instances. */
  private CurrenciesRequest(Builder builder) {
    this.showAlternative = builder.showAlternative;
    this.showInactive = builder.showInactive;
  }

  /**
   * Gets the showAlternative flag.
   *
   * @return True to include alternative currencies
   */
  public boolean getShowAlternative() {
    return showAlternative;
  }

  /**
   * Gets the showInactive flag.
   *
   * @return True to include historical/inactive currencies
   */
  public boolean getShowInactive() {
    return showInactive;
  }

  /**
   * Creates a new builder for constructing CurrenciesRequest instances.
   *
   * @return A new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a default CurrenciesRequest with no special parameters.
   *
   * @return A new CurrenciesRequest instance
   */
  public static CurrenciesRequest create() {
    return new Builder().build();
  }

  /** Builder for CurrenciesRequest. */
  public static class Builder {
    private boolean showAlternative = false;
    private boolean showInactive = false;

    private Builder() {}

    /**
     * Sets the showAlternative flag to include unofficial, black market and alternative digital
     * currencies.
     *
     * @param showAlternative True to include alternative currencies
     * @return This builder
     */
    public Builder setShowAlternative(boolean showAlternative) {
      this.showAlternative = showAlternative;
      return this;
    }

    /**
     * Sets the showInactive flag to include historical/inactive currencies.
     * digital currencies.
     *
     * @param showInactive True to include historical/inactive currencies
     * @return This builder
     */
    public Builder setShowInactive(boolean showInactive) {
      this.showInactive = showInactive;
      return this;
    }

    /**
     * Builds the CurrenciesRequest instance.
     *
     * @return A new CurrenciesRequest instance
     */
    public CurrenciesRequest build() {
      return new CurrenciesRequest(this);
    }
  }
}

