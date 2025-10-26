package org.freedger.service.models;

import java.time.Instant;
import java.time.LocalDate;

import org.freedger.repository.ditto.models.CurrencyType;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CurrencyState {
  @NonNull
  private String code;

  @NonNull
  private String name;

  private int decimalPlaces;

  private boolean enabled;

  @NonNull
  private CurrencyType currencyType;

  @Nullable
  private String instrumentId;
  
  @Nullable
  private Instant earliestInstant;

  @Nullable
  private Instant latestInstant;

  @Nullable
  private LocalDate earliestDate;

  @Nullable
  private LocalDate latestDate;
}
