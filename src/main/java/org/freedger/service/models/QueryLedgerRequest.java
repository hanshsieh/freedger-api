package org.freedger.service.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class QueryLedgerRequest {
  @NonNull
  String userId;
  String transactionId;
}
