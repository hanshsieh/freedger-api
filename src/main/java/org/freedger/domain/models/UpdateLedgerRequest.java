package org.freedger.domain.models;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

@Value
@Builder
public class UpdateLedgerRequest {
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
  private List<String> readerIds;
  @NonNull
  private List<String> writerIds;
  @NonNull
  private String externalAccountId;
  private String transactionId;
}
