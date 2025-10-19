package org.freedger.services.ditto.models;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class CreateQuoteRequest {
  @NotNull
  private String instrumentId;

  @NotNull
  private Instant time;

  @NotNull
  private BigDecimal value;

  @Nullable
  private String source;

  public String getInstrumentId() {
    return instrumentId;
  }

  public void setInstrumentId(String instrumentId) {
    this.instrumentId = instrumentId;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
