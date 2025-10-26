package org.freedger.repository.ditto.models;

import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateLedgerRequest {
  @NonNull
  private String name;

  @NonNull
  private String note;

  @NonNull
  private String currencyId;

  @NonNull
  private List<String> writerIds;

  @NonNull
  private List<String> readerIds;

  @NonNull
  private String externalAccountName;
}
