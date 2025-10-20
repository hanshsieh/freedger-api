package org.freedger.domain.models;

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
  private String externalAccountName;

  @NonNull
  private String userId;
}
