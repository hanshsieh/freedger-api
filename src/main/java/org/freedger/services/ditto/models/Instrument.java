package org.freedger.services.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

public class Instrument {
  private LedgerChildId id;
  private Instant createdAt;
  private Instant updatedAt;
  private String symbol;
  private String name;
  private InstrumentCategory category;
  private int decimals;
  private String quoteCurrencyId;
  private BigDecimal initialQuote;
}
