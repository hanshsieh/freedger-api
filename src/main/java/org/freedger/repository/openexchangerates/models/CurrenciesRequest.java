package org.freedger.repository.openexchangerates.models;

import lombok.Builder;
import lombok.Value;

/**
 * Request model for fetching currency list.
 *
 * <p>Use the builder pattern to construct requests:
 *
 * <pre>
 * CurrenciesRequest request = CurrenciesRequest.builder()
 *     .showAlternative(true)
 *     .showInactive(true)
 *     .build();
 * </pre>
 */
@Value
@Builder
public class CurrenciesRequest {
  /**
   * Include alternative currencies. Default is false.
   */
  @Builder.Default
  private boolean showAlternative = false;

  /**
   * Include historical/inactive currencies. Default is false.
   */
  @Builder.Default
  private boolean showInactive = false;
}

