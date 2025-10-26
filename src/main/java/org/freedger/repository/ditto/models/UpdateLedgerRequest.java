package org.freedger.repository.ditto.models;

import jakarta.annotation.Nullable;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class UpdateLedgerRequest {
  @Nullable
  private String transactionId;

  @NonNull
  private String id;

  @NonNull
  private String userId;

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
  private String externalAccountId;
}
